package org.ktb.matajo.service.notification;

import org.ktb.matajo.dto.notification.NotificationResponseDto;
import org.ktb.matajo.entity.ChatMessage;

import java.util.List;

public interface NotificationService {
    // 채팅 알림 전송
    void sendChatNotification(ChatMessage message, Long currentUserId);

    // 사용자의 읽지 않은 알림 조회
    List<NotificationResponseDto> getNotificationsForUser(Long userId);

    // 사용자의 모든 알림을 읽음 상태로 변경
    void markNotificationsAsRead(Long userId);
}