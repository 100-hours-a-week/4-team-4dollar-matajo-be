package org.ktb.matajo.service.location;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.entity.LocationInfo;
import org.ktb.matajo.repository.LocationInfoRepository;
import org.ktb.matajo.dto.location.LocationIdResponseDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationInfoServiceImpl implements LocationInfoService{
  private final LocationInfoRepository locationInfoRepository;

  @Override
  //@Cacheable(value = "locationCache", key = "#dongName + '_' + #guName")
  public List<LocationInfo> findLocationInfo(String dongName, String guName) {
    log.debug("위치 정보 검색 시작: dongName={}, guName={}", dongName, guName);

    // 1. 동 이름 기반 정확한 매칭 검색
    if (dongName != null && !dongName.isBlank()) {
      Optional<LocationInfo> exactMatch = locationInfoRepository.findByOriginalName(dongName);
      if (exactMatch.isPresent()) {
        log.debug("동 이름 정확 매칭 결과 발견: {}", dongName);
        return Collections.singletonList(exactMatch.get());
      }

            /*
            // 동 이름과 구 이름으로 조합 검색 (동명이 중복될 수 있는 경우)
            if (guName != null && !guName.isBlank()) {
                Optional<LocationInfo> combinedMatch =
                    locationInfoRepository.findByOriginalNameAndCityDistrictContaining(dongName, guName);
                if (combinedMatch.isPresent()) {
                    log.debug("동+구 조합 매칭 결과 발견: {}+{}", dongName, guName);
                    return Collections.singletonList(combinedMatch.get());
                }
            }*/
    }

    // 2. 구 이름 기반 검색
    if (guName != null && !guName.isBlank()) {
      Optional<LocationInfo> guMatch = locationInfoRepository.findFirstByCityDistrictContaining(guName);
      if (guMatch.isPresent()) {
        log.debug("구 이름 매칭 결과 발견: {}", guName);
        return Collections.singletonList(guMatch.get());
      }
    }

    log.debug("위치 정보 검색 결과 없음: dongName={}, guName={}", dongName, guName);
    return Collections.emptyList();
  }

  @Override
  public List<String> searchLocations(String searchTerm) {
    log.debug("위치 검색 시작: searchTerm={}", searchTerm);

    if (searchTerm == null || searchTerm.isBlank()) {
      log.debug("검색어가 비어있어 빈 결과 반환");
      return Collections.emptyList();
    }

    // 최대 20개 결과로 제한
    Pageable limit = PageRequest.of(0, 20);

    return locationInfoRepository.searchByDisplayNameOrderByPriority(searchTerm, limit)
        .stream()
        .map(LocationInfo::getFormattedAddress)
        .collect(Collectors.toList());
  }

  @Override
  public List<LocationIdResponseDto> findLocationByAddress(String formattedAddress) {
    log.debug("주소로 위치 ID 정보 조회 시작: formattedAddress={}", formattedAddress);
  
    if (formattedAddress == null || formattedAddress.isBlank()) {
        log.debug("주소가 비어있어 빈 결과 반환");
        return Collections.emptyList();
    }
  
    return locationInfoRepository.findByFormattedAddressContaining(formattedAddress)
        .stream()
        .map(locationInfo -> LocationIdResponseDto.builder()
            .id(locationInfo.getId())
            .latitude(locationInfo.getLatitude())
            .longitude(locationInfo.getLongitude())
            .build())
        .collect(Collectors.toList());
  }

}
