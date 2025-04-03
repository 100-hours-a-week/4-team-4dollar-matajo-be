package org.ktb.matajo.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.chat.ChatMessageResponseDto;
import org.ktb.matajo.dto.chat.ChatRoomDetailResponseDto;
import org.ktb.matajo.repository.UserRepository;
import org.ktb.matajo.service.chat.ChatRoomService;
import org.ktb.matajo.service.chat.ChatSessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        try {
            // 채팅방 상세 정보 조회
            ChatRoomDetailResponseDto roomDetail = chatRoomService.getChatRoomDetail(
                    currentUserId,
                    messageDto.getRoomId()
            );

            // 상대방 ID 확인
            Long receiverId = currentUserId.equals(roomDetail.getKeeperId())
                    ? roomDetail.getClientId()
                    : roomDetail.getKeeperId();

            // 상대방 닉네임 확인
            String receiverNickname = currentUserId.equals(roomDetail.getKeeperId())
                    ? roomDetail.getClientNickname()
                    : roomDetail.getKeeperNickname();

            // 상대방 FCM 토큰 조회
            String receiverFcmToken = userRepository.findById(receiverId)
                    .map(user -> user.getFcmToken())
                    .orElse(null);

            // FCM 알림 전송 (사용자가 해당 채팅방에 접속해 있지 않은 경우에만)
            Set<Long> activeUsersInRoom = chatSessionService.getActiveUsersInRoom(messageDto.getRoomId());
            
            if (!activeUsersInRoom.contains(receiverId) && receiverFcmToken != null) {
                // Firebase 서비스로 푸시 알림 전송
                firebaseNotificationService.sendMessageNotification(
                    messageDto.getSenderNickname(),
                    messageDto,
                    receiverFcmToken
                );
                log.debug("FCM 알림 전송 완료: receiverId={}", receiverId);
            } else {
                log.debug("FCM 알림 전송 생략 (사용자가 채팅방에 접속해 있거나 FCM 토큰 없음): receiverId={}, activeInRoom={}, hasFcmToken={}",
                        receiverId, activeUsersInRoom.contains(receiverId), receiverFcmToken != null);
            }
        } catch (Exception e) {
            log.error("알림 전송 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}