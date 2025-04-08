package org.ktb.matajo.service.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.chat.ChatMessageRequestDto;
import org.ktb.matajo.dto.chat.ChatMessageResponseDto;
import org.ktb.matajo.entity.*;
import org.ktb.matajo.global.error.code.ErrorCode;
import org.ktb.matajo.global.error.exception.BusinessException;
import org.ktb.matajo.repository.ChatMessageRepository;
import org.ktb.matajo.repository.ChatRoomRepository;
import org.ktb.matajo.repository.UserRepository;
import org.ktb.matajo.service.notification.NotificationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
//    private final RedisChatMessageService redisChatMessageService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 채팅 메시지 저장
     */
    @Override
    @Transactional
    public ChatMessageResponseDto saveMessage(Long roomId, ChatMessageRequestDto messageDto) {
        // 입력 유효성 검사
        validateMessageInput(roomId, messageDto);

        // 채팅방 조회
        ChatRoom chatRoom = findChatRoom(roomId);

        // 발신자 조회
        User sender = findSender(messageDto.getSenderId());

        log.info("메시지 생성 전 readStatus 설정: false");

        // 메시지 생성 및 저장
        ChatMessage chatMessage = createAndSaveChatMessage(chatRoom, sender, messageDto);

        // 저장 후 상태 확인
        log.info("메시지 ID: {}, 저장 후 readStatus: {}", chatMessage.getId(), chatMessage.isReadStatus());

        // 응답 DTO 생성
        ChatMessageResponseDto responseDto = convertToChatMessageResponseDto(chatMessage);

        // 캐시 및 알림 처리 (비동기적이고 독립적인 처리)
        handleCacheAndNotification(roomId, responseDto, sender);

        return responseDto;
    }

    /**
     * 입력 유효성 검사
     */
    private void validateMessageInput(Long roomId, ChatMessageRequestDto messageDto) {
        validateRoomId(roomId);

        // 이미지 타입 메시지 검증
        if (messageDto.isImageTypeWithEmptyContent()) {
            log.error("이미지 타입 메시지의 내용이 비어있습니다");
            throw new BusinessException(ErrorCode.INVALID_IMAGE_CONTENT);
        }
    }

    /**
     * 채팅방 조회
     */
    private ChatRoom findChatRoom(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> {
                    log.error("채팅방을 찾을 수 없습니다");
                    return new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND);
                });
    }

    /**
     * 발신자 조회
     */
    private User findSender(Long senderId) {
        return userRepository.findById(senderId)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없습니다");
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });
    }

    /**
     * 채팅 메시지 생성 및 저장
     */
    private ChatMessage createAndSaveChatMessage(ChatRoom chatRoom, User sender, ChatMessageRequestDto messageDto) {
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .user(sender)
                .content(messageDto.getContent())
                .messageType(messageDto.getMessageType())
                .readStatus(false)
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();

        return chatMessageRepository.save(chatMessage);
    }

    /**
     * 캐시 및 알림 처리
     */
    private void handleCacheAndNotification(Long roomId, ChatMessageResponseDto responseDto, User sender) {
        // 캐시 처리
//        try {
//            redisChatMessageService.cacheMessage(roomId, responseDto);
//        } catch (Exception e) {
//            log.warn("메시지 캐싱 실패: {}", e.getMessage());
//        }

        // 알림 처리
        try {
            notificationService.sendChatNotification(responseDto, sender.getId());
        } catch (Exception e) {
            log.warn("채팅 알림 전송 실패: {}", e.getMessage());
        }
    }

    /**
     * 채팅방의 메시지 목록 조회
     */
    @Override
    public List<ChatMessageResponseDto> getChatMessages(Long roomId) {

        validateRoomId(roomId);

        // 첫 페이지이면 캐시에서 먼저 조회 시도
//        if (page == 0) {
//            List<ChatMessageResponseDto> cachedMessages = redisChatMessageService.getCachedMessages(roomId, size);
//
//            if (!cachedMessages.isEmpty()) {
//                log.info("Redis 캐시에서 메시지 조회: roomId={}, cachedCount={}", roomId, cachedMessages.size());
//                return cachedMessages;
//            }
//        }

        // 메시지 조회
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomId(roomId);

        List<ChatMessageResponseDto> messageDtos = messages.stream()
                .map(this::convertToChatMessageResponseDto)
                .collect(Collectors.toList());

        // 첫 페이지 결과를 Redis에 캐싱 - 예외 처리 추가
//        if (page == 0 && !messageDtos.isEmpty()) {
//            try {
//                redisChatMessageService.cacheMessages(roomId, messageDtos);
//            } catch (Exception e) {
//                log.warn("메시지 목록 캐싱 실패 (무시됨): {}", e.getMessage());
//            }
//        }

        return messageDtos;
    }

    /**
     * 메시지 읽음 상태 업데이트
     */
    @Override
    @Transactional
    public void markMessagesAsRead(Long roomId, Long userId) {

        validateRoomId(roomId);

        // 파라미터 검증
        if (userId == null || userId <= 0) {
            log.error("유효하지 않은 userId 값입니다: {}", userId);
            throw new BusinessException(ErrorCode.INVALID_USER_ID);
        }

        // 읽지 않은 메시지 중 다른 사용자가 보낸 메시지만 읽음 처리
        List<ChatMessage> unreadMessages = chatMessageRepository.findUnreadMessagesForUser(roomId, userId);

        for (ChatMessage message : unreadMessages) {
            message.updateReadStatus(true);
        }

        chatMessageRepository.saveAll(unreadMessages);

        // Redis 캐시 업데이트 (캐시 무효화) - 예외 처리 추가
//        try {
//            redisChatMessageService.invalidateCache(roomId);
//        } catch (Exception e) {
//            log.warn("캐시 무효화 실패 (무시됨): {}", e.getMessage());
//        }

        // 읽음 처리된 메시지 ID 목록 수집
        List<Long> readMessageIds = unreadMessages.stream()
                .map(ChatMessage::getId)
                .collect(Collectors.toList());

        // 메시지 상태 업데이트 후 WebSocket으로 브로드캐스트
        if (!readMessageIds.isEmpty()) {
            Map<String, Object> readStatusUpdate = new HashMap<>();
            readStatusUpdate.put("type", "READ_STATUS_UPDATE");
            readStatusUpdate.put("roomId", roomId);
            readStatusUpdate.put("readBy", userId);
            readStatusUpdate.put("messageIds", readMessageIds);

            messagingTemplate.convertAndSend("/topic/chat/" + roomId + "/status", readStatusUpdate);

            // 안 읽은 메시지 개수 업데이트 정보도 브로드캐스트
            Long unreadCount = chatMessageRepository.countUnreadMessages(roomId, userId);
            Map<String, Object> unreadCountUpdate = new HashMap<>();
            unreadCountUpdate.put("type", "UNREAD_COUNT_UPDATE");
            unreadCountUpdate.put("roomId", roomId);
            unreadCountUpdate.put("userId", userId);
            unreadCountUpdate.put("unreadCount", unreadCount);

            messagingTemplate.convertAndSend("/topic/chat/unread", unreadCountUpdate);
        }
    }

    /**
     * ChatMessage 엔티티를 ChatMessageResponseDto로 변환
     */
    private ChatMessageResponseDto convertToChatMessageResponseDto(ChatMessage message) {
        return ChatMessageResponseDto.builder()
                .messageId(message.getId())
                .roomId(message.getChatRoom().getId())
                .senderId(message.getUser().getId())
                .senderNickname(message.getUser().getNickname())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .readStatus(message.isReadStatus())
                .createdAt(message.getCreatedAt())
                .build();
    }

    private void validateRoomId(Long roomId) {
        // 간단한 파라미터 검증
        if (roomId == null) {
            log.error("roomId가 null입니다");
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (roomId <= 0) {
            log.error("유효하지 않은 roomId 값입니다: {}", roomId);
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}