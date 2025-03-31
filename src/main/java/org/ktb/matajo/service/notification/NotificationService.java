package org.ktb.matajo.service.notification;

import org.ktb.matajo.dto.notification.NotificationResponseDto;
import org.ktb.matajo.entity.ChatMessage;

import java.util.List;

public interface NotificationService {
    void sendChatNotification(ChatMessage message, Long currentUserId);
    List<NotificationResponseDto> getNotificationsForUser(Long userId);
    void markNotificationsAsRead(Long userId);
}