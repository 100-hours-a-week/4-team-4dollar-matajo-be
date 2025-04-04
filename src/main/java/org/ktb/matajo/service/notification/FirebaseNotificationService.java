package org.ktb.matajo.service.notification;

import org.ktb.matajo.dto.chat.ChatMessageResponseDto;
import org.ktb.matajo.dto.notification.FcmNotificationRequestDto;

import java.util.Map;

/**
 * Firebase Cloud Messaging(FCM) 알림 서비스
 */
public interface FirebaseNotificationService {

    /**
     * 채팅 메시지에 대한 푸시 알림 전송
     * 
     * @param senderNickname 발신자 닉네임
     * @param messageDto 채팅 메시지 응답 DTO
     * @param fcmToken 수신자의 FCM 토큰
     */
    void sendMessageNotification(String senderNickname, ChatMessageResponseDto messageDto, String fcmToken);

}