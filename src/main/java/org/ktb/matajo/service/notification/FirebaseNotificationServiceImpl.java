package org.ktb.matajo.service.notification;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.chat.ChatMessageResponseDto;
import org.ktb.matajo.entity.MessageType;
import org.ktb.matajo.global.error.code.ErrorCode;
import org.ktb.matajo.global.error.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Firebase Cloud Messaging(FCM)을 통한 푸시 알림 서비스 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseNotificationServiceImpl implements FirebaseNotificationService {

    // Firebase 메시징 서비스 주입
    private final FirebaseMessaging firebaseMessaging;

    // 알림 아이콘 경로 설정값
    @Value("${firebase.notification.icon}")
    private String notificationIcon;

    // 알림 색상 설정값
    @Value("${firebase.notification.color}")
    private String notificationColor;

    /**
     * 채팅 메시지에 대한 푸시 알림 전송
     *
     * @param senderNickname 발신자 닉네임
     * @param messageDto 채팅 메시지 응답 DTO
     * @param fcmToken 수신자의 FCM 토큰
     */
    @Override
    public void sendMessageNotification(String senderNickname, ChatMessageResponseDto messageDto, String fcmToken) {
        // 입력 유효성 검사
        validateNotificationInput(senderNickname, messageDto, fcmToken);

        try {
            // 메시지 유형에 따른 알림 내용 포맷팅
            String notificationContent = formatNotificationContent(messageDto);

            // Firebase 알림 생성
            Notification notification = Notification.builder()
                    .setTitle(senderNickname)
                    .setBody(notificationContent)
                    .build();

            // 알림과 함께 전달할 추가 데이터 생성
            Map<String, String> dataPayload = createDataPayload(messageDto, senderNickname);

            // Firebase 메시지 구성
            Message fcmMessage = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(notification)
                    .putAllData(dataPayload)
                    .build();

            // 알림 비동기 전송
            String response = sendFcmMessage(fcmMessage);
            log.info("FCM 알림 전송 성공: {}", response);

        } catch (BusinessException e) {
            log.error("알림 전송 중 비즈니스 예외 발생: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("FCM 알림 전송 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FAILED_TO_SEND_NOTIFICATION);
        }
    }

    /**
     * 입력 유효성 검사
     */
    private void validateNotificationInput(String senderNickname, ChatMessageResponseDto messageDto, String fcmToken) {
        if (senderNickname == null || senderNickname.isBlank()) {
            log.warn("알림 전송 실패: 발신자 닉네임이 유효하지 않습니다.");
            throw new BusinessException(ErrorCode.NOTIFICATION_MESSAGE_INVALID);
        }

        if (messageDto == null) {
            log.warn("알림 전송 실패: 메시지 DTO가 null입니다.");
            throw new BusinessException(ErrorCode.NOTIFICATION_MESSAGE_INVALID);
        }

        if (fcmToken == null || fcmToken.isBlank()) {
            log.warn("알림 전송 실패: FCM 토큰이 유효하지 않습니다.");
            throw new BusinessException(ErrorCode.NOTIFICATION_MESSAGE_INVALID);
        }
    }

    /**
     * Firebase 메시지 전송
     */
    private String sendFcmMessage(Message fcmMessage) throws BusinessException {
        try {
            return firebaseMessaging.sendAsync(fcmMessage).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("FCM 알림 전송이 중단되었습니다: {}", e.getMessage());
            throw new BusinessException(ErrorCode.FAILED_TO_SEND_NOTIFICATION);
        } catch (ExecutionException e) {
            log.error("FCM 알림 전송 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FAILED_TO_SEND_NOTIFICATION);
        }
    }

    /**
     * 메시지 유형에 따른 알림 내용 포맷팅
     */
    private String formatNotificationContent(ChatMessageResponseDto messageDto) {
        if (messageDto.getMessageType() == MessageType.IMAGE) {
            return "사진을 보냈습니다.";
        } else if (messageDto.getMessageType() == MessageType.SYSTEM) {
            return messageDto.getContent();
        } else {
            // TEXT 유형인 경우 내용 길이 제한
            return messageDto.getContent().length() > 50
                    ? messageDto.getContent().substring(0, 47) + "..."
                    : messageDto.getContent();
        }
    }

    /**
     * 알림 데이터 페이로드 생성
     */
    private Map<String, String> createDataPayload(ChatMessageResponseDto messageDto, String senderNickname) {
        Map<String, String> dataPayload = new HashMap<>();
        dataPayload.put("roomId", messageDto.getRoomId().toString());
        dataPayload.put("senderId", messageDto.getSenderId().toString());
        dataPayload.put("senderNickname", senderNickname);
        dataPayload.put("messageType", messageDto.getMessageType().toString());
        dataPayload.put("messageId", messageDto.getMessageId().toString());
        dataPayload.put("clickAction", "OPEN_CHAT_ROOM");
        return dataPayload;
    }
}