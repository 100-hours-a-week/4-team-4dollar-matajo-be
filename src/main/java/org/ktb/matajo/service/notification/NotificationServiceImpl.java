package org.ktb.matajo.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.chat.ChatMessageResponseDto;
import org.ktb.matajo.dto.chat.ChatRoomDetailResponseDto;
import org.ktb.matajo.entity.User;
import org.ktb.matajo.global.error.code.ErrorCode;
import org.ktb.matajo.global.error.exception.BusinessException;
import org.ktb.matajo.repository.UserRepository;
import org.ktb.matajo.service.chat.ChatRoomService;
import org.ktb.matajo.service.chat.ChatSessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {
    private final ChatRoomService chatRoomService;
    private final ChatSessionService chatSessionService;
    private final UserRepository userRepository;
    private final FirebaseNotificationService firebaseNotificationService;

    @Override
    public void sendChatNotification(ChatMessageResponseDto messageDto, Long currentUserId) {
        // 입력 유효성 검사
        validateNotificationInput(messageDto, currentUserId);

        try {
            // 채팅방 상세 정보 조회
            ChatRoomDetailResponseDto roomDetail = getChatRoomDetail(messageDto, currentUserId);

            // 수신자 정보 조회
            User receiverUser = findReceiverUser(roomDetail, currentUserId);

            // 알림 전송 조건 확인
            if (shouldSendNotification(messageDto, receiverUser)) {
                sendFirebaseNotification(messageDto, receiverUser);
            } else {
                log.debug("FCM 알림 전송 생략: receiverId={}, activeInRoom={}, hasFcmToken={}",
                        receiverUser.getId(),
                        isReceiverActiveInRoom(messageDto, receiverUser.getId()),
                        receiverUser.getFcmToken() != null
                );
            }
        } catch (BusinessException e) {
            log.error("알림 전송 중 비즈니스 예외 발생: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("알림 전송 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FAILED_TO_SEND_NOTIFICATION);
        }
    }

    // 입력 유효성 검사
    private void validateNotificationInput(ChatMessageResponseDto messageDto, Long currentUserId) {
        if (messageDto == null) {
            log.warn("알림 전송 실패: 메시지가 null입니다.");
            throw new BusinessException(ErrorCode.NOTIFICATION_MESSAGE_INVALID);
        }

        if (currentUserId == null) {
            log.warn("알림 전송 실패: 사용자 ID가 null입니다.");
            throw new BusinessException(ErrorCode.NOTIFICATION_MESSAGE_INVALID);
        }
    }

    // 채팅방 상세 정보 조회
    private ChatRoomDetailResponseDto getChatRoomDetail(ChatMessageResponseDto messageDto, Long currentUserId) {
        try {
            return chatRoomService.getChatRoomDetail(currentUserId, messageDto.getRoomId());
        } catch (Exception e) {
            log.error("채팅방 상세 정보 조회 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND);
        }
    }

    // 수신자 사용자 조회
    private User findReceiverUser(ChatRoomDetailResponseDto roomDetail, Long currentUserId) {
        // 상대방 ID 확인 (발신자와 다른 사용자)
        Long receiverId = currentUserId.equals(roomDetail.getKeeperId())
                ? roomDetail.getClientId()
                : roomDetail.getKeeperId();

        return userRepository.findById(receiverId)
                .orElseThrow(() -> {
                    log.warn("알림 전송 실패: 수신자 사용자를 찾을 수 없습니다. userId={}", receiverId);
                    return new BusinessException(ErrorCode.NOTIFICATION_RECEIVER_NOT_FOUND);
                });
    }

    // 알림 전송 조건 확인
    private boolean shouldSendNotification(ChatMessageResponseDto messageDto, User receiverUser) {
        // 조건 1: 수신자가 채팅방에 없음
        boolean isReceiverNotActive = !isReceiverActiveInRoom(messageDto, receiverUser.getId());

        // 조건 2: FCM 토큰이 유효함
        boolean hasValidFcmToken = receiverUser.getFcmToken() != null
                && !receiverUser.getFcmToken().isBlank();

        return isReceiverNotActive && hasValidFcmToken;
    }

    // 수신자가 채팅방에 활성화되어 있는지 확인
    private boolean isReceiverActiveInRoom(ChatMessageResponseDto messageDto, Long receiverId) {
        Set<Long> activeUsersInRoom = chatSessionService.getActiveUsersInRoom(messageDto.getRoomId());
        return activeUsersInRoom.contains(receiverId);
    }

    // Firebase 알림 전송
    private void sendFirebaseNotification(ChatMessageResponseDto messageDto, User receiverUser) {
        try {
            firebaseNotificationService.sendMessageNotification(
                    messageDto.getSenderNickname(),
                    messageDto,
                    receiverUser.getFcmToken()
            );
            log.info("FCM 알림 전송 성공: receiverId={}, senderNickname={}",
                    receiverUser.getId(), messageDto.getSenderNickname());
        } catch (Exception e) {
            log.error("Firebase 알림 전송 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FAILED_TO_SEND_NOTIFICATION);
        }
    }
}