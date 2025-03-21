package org.ktb.matajo.repository;

import org.ktb.matajo.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    // 동일한 주소가 있는지 확인하기 위한 메소드
    Optional<Address> findByZonecodeAndAddress(String zonecode, String address);
}
