package org.ktb.matajo.repository;

import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import org.ktb.matajo.entity.LocationInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

public interface LocationInfoRepository extends JpaRepository<LocationInfo, Long> {

    // 동 이름으로 검색
    Optional<LocationInfo> findByOriginalName(String originalName);

    // 구 이름으로 검색 (가장 첫번째에 있는 구)
    Optional<LocationInfo> findFirstByCityDistrictContaining(String cityDistrict);

    //검색어로 동 검색
    @Query(value = """
            SELECT l FROM LocationInfo l
            WHERE l.displayName LIKE CONCAT('%', :searchTerm, '%')
            ORDER BY 
                CASE 
                    WHEN l.displayName LIKE CONCAT(:searchTerm, '%') THEN 1
                    ELSE 2
                END,
                l.displayName
            """)
    List<LocationInfo> searchByDisplayNameOrderByPriority(
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

}
