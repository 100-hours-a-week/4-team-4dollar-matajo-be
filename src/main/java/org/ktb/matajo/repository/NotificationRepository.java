package org.ktb.matajo.repository;

import org.ktb.matajo.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 사용자의 읽지 않은 알림을 최신 순으로 조회
    List<Notification> findByReceiverIdAndReadStatusOrderByCreatedAtDesc(Long userId, boolean readStatus);

    // 사용자의 읽지 않은 알림 개수 카운트
    long countByReceiverIdAndReadStatus(Long userId, boolean readStatus);
}