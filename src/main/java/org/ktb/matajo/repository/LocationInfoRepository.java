package org.ktb.matajo.repository;

import org.ktb.matajo.entity.LocationInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationInfoRepository extends JpaRepository<LocationInfo, Long> {

    // 동 이름으로 검색
    Optional<LocationInfo> findByOriginalName(String originalName);

    // 구 이름으로 검색 (가장 첫번째에 있는 구)
    Optional<LocationInfo> findFirstByCityDistrictContaining(String cityDistrict);

}
