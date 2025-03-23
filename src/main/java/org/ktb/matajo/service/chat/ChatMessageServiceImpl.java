package org.ktb.matajo.service.chat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import org.ktb.matajo.dto.chat.ChatMessageRequestDto;
import org.ktb.matajo.dto.chat.ChatMessageResponseDto;
import org.ktb.matajo.entity.*;
import org.ktb.matajo.repository.ChatMessageRepository;
import org.ktb.matajo.repository.ChatRoomRepository;
import org.ktb.matajo.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final RedisChatMessageService redisChatMessageService;

    /** 채팅 메시지 저장 */
    @Override
    @Transactional
    public ChatMessageResponseDto saveMessage(Long roomId, ChatMessageRequestDto messageDto) {

        log.info("메시지 요청 DTO: {}", messageDto);
        log.info(
                "senderId: {}, 타입: {}",
                messageDto.getSenderId(),
                messageDto.getSenderId() != null ? messageDto.getSenderId().getClass().getName() : "null");

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다. ID: " + roomId));

        User sender = userRepository.findById(messageDto.getSenderId())
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + messageDto.getSenderId()));

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .user(sender)
                .content(messageDto.getContent())
                .messageType(messageDto.getMessageType())
                .readStatus(false)
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        ChatMessageResponseDto responseDto = convertToChatMessageResponseDto(savedMessage);

        redisChatMessageService.cacheMessage(roomId, responseDto);

        return responseDto;
    }

    /** 채팅방의 메시지 목록 조회 */
    @Override
    public List<ChatMessageResponseDto> getChatMessages(Long roomId, int page, int size) {
        if (page == 0) {
            List<ChatMessageResponseDto> cachedMessages = redisChatMessageService.getCachedMessages(roomId, size);
            if (!cachedMessages.isEmpty()) {
                log.info("Redis 캐시에서 메시지 조회: roomId={}, cachedCount={}", roomId, cachedMessages.size());
                return cachedMessages;
            }
        }

        log.info("DB에서 메시지 조회: roomId={}, page={}, size={}", roomId, page, size);

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomId(roomId, pageRequest);

        List<ChatMessageResponseDto> messageDtos = messages.stream()
                .map(this::convertToChatMessageResponseDto)
                .collect(Collectors.toList());

        if (page == 0 && !messageDtos.isEmpty()) {
            redisChatMessageService.cacheMessages(roomId, messageDtos);
        }

        return messageDtos;
    }

    /** 메시지 읽음 상태 업데이트 */
    @Override
    @Transactional
    public void markMessagesAsRead(Long roomId, Long userId) {
        List<ChatMessage> unreadMessages = chatMessageRepository.findUnreadMessagesForUser(roomId, userId);

        for (ChatMessage message : unreadMessages) {
            message.updateReadStatus(true);
        }

        chatMessageRepository.saveAll(unreadMessages);
        redisChatMessageService.invalidateCache(roomId);
    }

    /** ChatMessage → ChatMessageResponseDto 변환 */
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
}
