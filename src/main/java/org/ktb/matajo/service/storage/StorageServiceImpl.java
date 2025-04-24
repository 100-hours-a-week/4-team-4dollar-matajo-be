package org.ktb.matajo.service.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.storage.StorageResponseDto;
import org.ktb.matajo.entity.Storage;
import org.ktb.matajo.global.error.code.ErrorCode;
import org.ktb.matajo.global.error.exception.BusinessException;
import org.ktb.matajo.repository.StorageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StorageServiceImpl implements StorageService {

    private final StorageRepository storageRepository;

    @Override
    public List<StorageResponseDto> getStoragesByLocation(Long locationInfoId) {
        if (locationInfoId == null) {
            throw new BusinessException(ErrorCode.INVALID_LOCATION_ID);
        }

        List<Storage> storages = storageRepository.findByLocationInfoId(locationInfoId);

        return storages.stream()
                .map(s -> new StorageResponseDto(
                        s.getId(),
                        s.getKakaoMapLink(),
                        s.getName(),
                        s.getLocationInfoId()
                ))
                .collect(Collectors.toList());
    }

}