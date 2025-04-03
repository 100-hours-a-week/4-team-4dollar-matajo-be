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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final RedisChatMessageService redisChatMessageService;
    private final NotificationService notificationService;
    private final ChatSessionService chatSessionService;

    /**
     * 채팅 메시지 저장
     */
    @Override
    @Transactional
    public ChatMessageResponseDto saveMessage(Long roomId, ChatMessageRequestDto messageDto) {

        validateRoomId(roomId);

        // DTO 추가 메서드를 활용한 검증
        if(messageDto.isImageTypeWithEmptyContent()) {
            log.error("이미지 타입 메시지의 내용이 비어있습니다");
            throw new BusinessException(ErrorCode.INVALID_IMAGE_CONTENT);
        }

        if(!messageDto.isValidImageUrl()) {
            log.error("유효하지 않은 이미지 URL 형식: {}", messageDto.getContent());
            throw new BusinessException(ErrorCode.INVALID_IMAGE_URL);
        }

        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> {
                    log.error("채팅방을 찾을 수 없습니다");
                    return new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND);
                });

        // 사용자 조회
        User sender = userRepository.findById(messageDto.getSenderId())
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없습니다");
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        // 채팅 메시지 생성
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .user(sender)
                .content(messageDto.getContent())
                .messageType(messageDto.getMessageType())
                .readStatus(false)
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();

        // DB에 저장
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        // 응답 DTO 생성
        ChatMessageResponseDto responseDto = convertToChatMessageResponseDto(savedMessage);

        // 안전한 캐싱 (예외가 발생해도 메인 로직에 영향 없게)
        try {
            redisChatMessageService.cacheMessage(roomId, responseDto);
        } catch (Exception e) {
            log.warn("메시지 캐싱 실패 (무시됨): {}", e.getMessage());
        }


        try {
            // 1대1 채팅이므로 수신자 ID 결정
            Long senderId = messageDto.getSenderId();
            Long receiverId;

            if (chatRoom.getPost().getUser().getId().equals(senderId)) {
                receiverId = chatRoom.getUser().getId();
            } else {
                receiverId = chatRoom.getPost().getUser().getId();
            }

            log.info("💬 채팅 메시지 전송: senderId={}, receiverId={}, roomId={}", senderId, receiverId, roomId);

            // 채팅방 현재 접속 중인 사용자 목록 확인
            Set<Long> activeUsersInRoom = chatSessionService.getActiveUsersInRoom(roomId);
            log.info("📌 현재 접속 중인 사용자: {}", activeUsersInRoom);

            if (!activeUsersInRoom.contains(receiverId)) {
                log.info("🔔 수신자가 채팅방에 없으므로 알림 발송: receiverId={}", receiverId);
                notificationService.sendChatNotification(savedMessage, senderId);
                log.info("✅ 알림 발송 성공: senderId={}, receiverId={}, messageId={}", senderId, receiverId, savedMessage.getId());
            } else {
                log.debug("❌ 수신자가 채팅방에 접속 중이므로 알림 발송 생략: receiverId={}", receiverId);
            }
        } catch (Exception e) {
            log.warn("⚠️ 알림 발송 중 오류 발생: {}", e.getMessage(), e);
        }


        return responseDto;
    }

    /**
     * 채팅방의 메시지 목록 조회
     */
    @Override
    public List<ChatMessageResponseDto> getChatMessages(Long roomId, int page, int size) {

        validateRoomId(roomId);

        if (page < 0 || size <= 0 || size > 100) {
            log.error("페이지네이션 파라미터가 유효하지 않습니다: page={}, size={}", page, size);
            throw new BusinessException(ErrorCode.INVALID_OFFSET_OR_LIMIT);
        }
        // 첫 페이지이면 캐시에서 먼저 조회 시도
        if (page == 0) {
            List<ChatMessageResponseDto> cachedMessages = redisChatMessageService.getCachedMessages(roomId, size);

            if (!cachedMessages.isEmpty()) {
                log.info("Redis 캐시에서 메시지 조회: roomId={}, cachedCount={}", roomId, cachedMessages.size());
                return cachedMessages;
            }
        }

        // 캐시에 없거나 첫 페이지가 아니면 DB에서 조회
        log.info("DB에서 메시지 조회: roomId={}, page={}, size={}", roomId, page, size);

        // 메시지 조회
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomId(roomId, pageRequest);

        List<ChatMessageResponseDto> messageDtos = messages.stream()
                .map(this::convertToChatMessageResponseDto)
                .collect(Collectors.toList());

        // 첫 페이지 결과를 Redis에 캐싱 - 예외 처리 추가
        if (page == 0 && !messageDtos.isEmpty()) {
            try {
                redisChatMessageService.cacheMessages(roomId, messageDtos);
            } catch (Exception e) {
                log.warn("메시지 목록 캐싱 실패 (무시됨): {}", e.getMessage());
            }
        }

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
        try {
            redisChatMessageService.invalidateCache(roomId);
        } catch (Exception e) {
            log.warn("캐시 무효화 실패 (무시됨): {}", e.getMessage());
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