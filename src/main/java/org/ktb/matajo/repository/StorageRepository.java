package org.ktb.matajo.repository;

import org.ktb.matajo.entity.Storage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StorageRepository extends JpaRepository<Storage, Long> {
    List<Storage> findByLocationInfoId(Long locationInfoId);
}
