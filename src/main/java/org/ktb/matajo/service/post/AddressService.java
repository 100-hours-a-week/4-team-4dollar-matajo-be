package org.ktb.matajo.service.post;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.post.AddressDto;
import org.ktb.matajo.entity.Address;
import org.ktb.matajo.entity.LocationInfo;
import org.ktb.matajo.service.location.LocationInfoService;
import org.ktb.matajo.global.error.code.ErrorCode;
import org.ktb.matajo.global.error.exception.BusinessException;
import org.ktb.matajo.repository.AddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    private final LocationInfoService locationInfoService;

    /**
     * 게시글 전용 주소 객체 생성 (일대일 관계 유지)
     */
    @Transactional
    public Address createAddressForPost(AddressDto addressDto) {
        // 유효성 검증
        if (addressDto == null || addressDto.getAddress() == null || addressDto.getAddress().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_POST_ADDRESS);
        }

        // 위치 정보 찾기 - 정적 호출 대신 인스턴스 메서드 호출
        List<LocationInfo> locationInfos = locationInfoService.findLocationInfo(
            addressDto.getBname(), addressDto.getSigungu());
            
        // 리스트에서 첫 번째 항목 추출 (비어있을 경우 null)
        LocationInfo locationInfo = locationInfos.isEmpty() ? null : locationInfos.get(0);

        log.info("게시글용 주소 정보 생성: {}", addressDto.getAddress());

        // 새 주소 엔티티 생성 및 저장
        Address address = Address.builder()
                .postcode(addressDto.getPostcode())
                .postcode1(addressDto.getPostcode1())
                .postcode2(addressDto.getPostcode2())
                .postcodeSeq(addressDto.getPostcodeSeq())
                .zonecode(addressDto.getZonecode())
                .address(addressDto.getAddress())
                .addressEnglish(addressDto.getAddressEnglish())
                .addressType(addressDto.getAddressType())
                .bcode(addressDto.getBcode())
                .bname(addressDto.getBname())
                .bnameEnglish(addressDto.getBnameEnglish())
                .bname1(addressDto.getBname1())
                .bname1English(addressDto.getBname1English())
                .bname2(addressDto.getBname2())
                .bname2English(addressDto.getBname2English())
                .sido(addressDto.getSido())
                .sidoEnglish(addressDto.getSidoEnglish())
                .sigungu(addressDto.getSigungu())
                .sigunguEnglish(addressDto.getSigunguEnglish())
                .sigunguCode(addressDto.getSigunguCode())
                .userLanguageType(addressDto.getUserLanguageType())
                .query(addressDto.getQuery())
                .buildingName(addressDto.getBuildingName())
                .buildingCode(addressDto.getBuildingCode())
                .apartment(addressDto.getApartment())
                .jibunAddress(addressDto.getJibunAddress())
                .jibunAddressEnglish(addressDto.getJibunAddressEnglish())
                .roadAddress(addressDto.getRoadAddress())
                .roadAddressEnglish(addressDto.getRoadAddressEnglish())
                .autoRoadAddress(addressDto.getAutoRoadAddress())
                .autoRoadAddressEnglish(addressDto.getAutoRoadAddressEnglish())
                .autoJibunAddress(addressDto.getAutoJibunAddress())
                .autoJibunAddressEnglish(addressDto.getAutoJibunAddressEnglish())
                .userSelectedType(addressDto.getUserSelectedType())
                .noSelected(addressDto.getNoSelected())
                .hname(addressDto.getHname())
                .roadnameCode(addressDto.getRoadnameCode())
                .roadname(addressDto.getRoadname())
                .roadnameEnglish(addressDto.getRoadnameEnglish())
                .locationInfo(locationInfo)
                .build();

        Address savedAddress = addressRepository.save(address);
        log.info("게시글 전용 주소 정보 저장 완료: ID={}", savedAddress.getId());

        return savedAddress;
    }

    /**
     * 기존 주소 정보 업데이트
     */
    @Transactional
    public Address updateAddress(Address address, AddressDto addressDto) {
        
        // 유효성 검증
        if (addressDto == null || addressDto.getAddress() == null || addressDto.getAddress().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_POST_ADDRESS);
        }
        
        log.info("주소 정보 업데이트: ID={}, 기존 주소={}, 새 주소={}", 
                address.getId(), address.getAddress(), addressDto.getAddress());
        
        // 주소 정보 직접 업데이트
        address.update(addressDto);
        
        // 관련 위치 정보 찾기 - 정적 호출 대신 인스턴스 메서드 호출
        List<LocationInfo> locationInfos = locationInfoService.findLocationInfo(
            addressDto.getBname(), addressDto.getSigungu());
            
        // 리스트에서 첫 번째 항목 추출 (비어있을 경우 null)
        LocationInfo locationInfo = locationInfos.isEmpty() ? null : locationInfos.get(0);
    
        // 위치 정보가 변경되었을 경우에만 업데이트
        if (locationInfo != null &&
            (address.getLocationInfo() == null ||
            !address.getLocationInfo().getId().equals(locationInfo.getId()))) {
        
            address.updateLocationInfo(locationInfo);
            log.info("주소 위치 정보 업데이트: addressId={}, locationId={}",
                    address.getId(), locationInfo.getId());
        }
        
        return address;
    }
}