package org.ktb.matajo.repository;

import org.ktb.matajo.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReceiverIdAndReadStatusOrderByCreatedAtDesc(Long userId, boolean readStatus);
    long countByReceiverIdAndReadStatus(Long userId, boolean readStatus);
}