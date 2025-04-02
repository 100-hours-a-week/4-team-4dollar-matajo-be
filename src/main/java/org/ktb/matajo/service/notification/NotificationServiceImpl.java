package org.ktb.matajo.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.chat.ChatRoomDetailResponseDto;
import org.ktb.matajo.dto.notification.NotificationResponseDto;
import org.ktb.matajo.entity.ChatMessage;
import org.ktb.matajo.entity.MessageType;
import org.ktb.matajo.entity.Notification;
import org.ktb.matajo.entity.User;
import org.ktb.matajo.global.error.code.ErrorCode;
import org.ktb.matajo.global.error.exception.BusinessException;
import org.ktb.matajo.repository.NotificationRepository;
import org.ktb.matajo.repository.UserRepository;
import org.ktb.matajo.service.chat.ChatRoomService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomService chatRoomService;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void sendChatNotification(ChatMessage message, Long currentUserId) {
        try {
            // 채팅방 상세 정보 조회
            ChatRoomDetailResponseDto roomDetail = chatRoomService.getChatRoomDetail(
                    currentUserId,
                    message.getChatRoom().getId()
            );

            // 상대방 ID 확인
            Long receiverId = currentUserId.equals(roomDetail.getKeeperId())
                    ? roomDetail.getClientId()
                    : roomDetail.getKeeperId();

            User receiver = userRepository.findById(receiverId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            // 알림 엔티티 생성 및 저장
            Notification notification = Notification.builder()
                    .receiver(receiver)
                    .senderId(message.getUser().getId())
                    .senderNickname(message.getUser().getNickname())
                    .chatRoomId(message.getChatRoom().getId())
                    .content(formatNotificationContent(message))
                    .readStatus(false)
                    .build();

            notificationRepository.save(notification);

            // 읽지 않은 알림 개수 계산
            long unreadCount = notificationRepository.countByReceiverIdAndReadStatus(receiverId, false);

            // 웹소켓 알림 전송
            NotificationResponseDto notificationDto = convertToDto(notification);
            notificationDto.setUnreadCount(unreadCount);

            messagingTemplate.convertAndSendToUser(
                    receiverId.toString(),
                    "/queue/notifications",
                    notificationDto
            );

            log.debug("채팅 알림 전송 완료: senderId={}, receiverId={}",
                    message.getUser().getId(), receiverId);

        } catch (Exception e) {
            log.error("알림 전송 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<NotificationResponseDto> getNotificationsForUser(Long userId) {
        List<Notification> notifications = notificationRepository
                .findByReceiverIdAndReadStatusOrderByCreatedAtDesc(userId, false);

        return notifications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markNotificationsAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository
                .findByReceiverIdAndReadStatusOrderByCreatedAtDesc(userId, false);

        unreadNotifications.forEach(Notification::markAsRead);
        notificationRepository.saveAll(unreadNotifications);
    }

    private NotificationResponseDto convertToDto(Notification notification) {
        return NotificationResponseDto.builder()
                .id(notification.getId())
                .chatRoomId(notification.getChatRoomId())
                .senderId(notification.getSenderId())
                .senderNickname(notification.getSenderNickname())
                .content(notification.getContent())
                .createdAt(notification.getCreatedAt())
                .readStatus(notification.isReadStatus())
                .build();
    }

    // 메시지 포맷팅 메서드
    private String formatNotificationContent(ChatMessage message) {
        if (message.getMessageType() == MessageType.IMAGE) {
            return "이미지를 보냈습니다.";
        }

        String content = message.getContent();
        return content.length() > 30 ?
                content.substring(0, 27) + "..." : content;
    }
}