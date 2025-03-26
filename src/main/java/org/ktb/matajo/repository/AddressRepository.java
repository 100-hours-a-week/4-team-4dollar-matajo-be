package org.ktb.matajo.repository;

import java.util.List;

import org.ktb.matajo.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

  // 위치 정보 ID로 주소 조회
  @Query("SELECT a FROM Address a WHERE a.locationInfo.id = :locationInfoId")
  List<Address> findByLocationInfoId(@Param("locationInfoId") Long locationInfoId);

  // 위치 정보 ID 목록으로 주소 ID 조회
  @Query("SELECT a.id FROM Address a WHERE a.locationInfo.id IN :locationInfoIds")
  List<Long> findAddressIdsByLocationInfoIds(@Param("locationInfoIds") List<Long> locationInfoIds);
}
