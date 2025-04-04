
package org.ktb.matajo.service.notification;

import org.ktb.matajo.dto.chat.ChatMessageResponseDto;

/**
 * 알림 관련 서비스 인터페이스
 */
public interface NotificationService {
    /**
     * 채팅 알림 전송
     * 
     * @param message 채팅 메시지 응답 DTO
     * @param currentUserId 현재 사용자 ID
     */
    void sendChatNotification(ChatMessageResponseDto message, Long currentUserId);
}