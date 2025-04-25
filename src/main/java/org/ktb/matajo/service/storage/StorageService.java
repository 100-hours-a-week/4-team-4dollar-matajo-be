package org.ktb.matajo.service.storage;

import org.ktb.matajo.dto.storage.StorageResponseDto;

import java.util.List;

public interface StorageService {
    List<StorageResponseDto> getStoragesByLocation(Long locationInfoId);
}
