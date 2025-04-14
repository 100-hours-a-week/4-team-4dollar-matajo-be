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

    //동일한 city_district를 가진 id 가져오기
    @Query("SELECT DISTINCT l2.id FROM LocationInfo l1 " +
        "JOIN LocationInfo l2 ON l1.cityDistrict = l2.cityDistrict " +
        "WHERE l1.id = :locationInfoId")
    List<Long> findIdsInSameDistrict(@Param("locationInfoId") Long locationInfoId);

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

    @Query("SELECT l FROM LocationInfo l " +
        "WHERE l.formattedAddress LIKE CONCAT('%', :formattedAddress, '%') " +
        "ORDER BY LENGTH(l.formattedAddress) ASC ")
    List<LocationInfo> findByFormattedAddressContaining(@Param("formattedAddress") String formattedAddress);

}
