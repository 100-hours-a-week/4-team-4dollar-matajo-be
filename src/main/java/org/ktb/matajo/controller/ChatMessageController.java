package org.ktb.matajo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.chat.ChatMessageRequestDto;
import org.ktb.matajo.dto.chat.ChatMessageResponseDto;
import org.ktb.matajo.entity.MessageType;
import org.ktb.matajo.global.common.CommonResponse;
import org.ktb.matajo.security.SecurityUtil;
import org.ktb.matajo.service.chat.ChatMessageService;
import org.ktb.matajo.service.chat.ChatSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@Slf4j
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatSessionService chatSessionService;

    /**
     * WebSocket을 통한 메시지 전송
     * /app/chat/{roomId} 엔드포인트로 메시지가 전송됨
     */
    @MessageMapping("/{roomId}/message")
    public void sendMessage(@DestinationVariable Long roomId,
                            @Valid @Payload ChatMessageRequestDto messageDto,
                            SimpMessageHeaderAccessor headerAccessor) {
        log.info("메시지 전송: roomId={}, senderId={}, type={}", roomId, messageDto.getSenderId(), messageDto.getMessageType());

        // 이미지 메시지 처리를 위한 로그 추가
        if (messageDto.getMessageType() == MessageType.IMAGE) {
            log.info("이미지 메시지 처리: content(URL)={}", messageDto.getContent());
        }

        // 메시지 저장 및 처리
        ChatMessageResponseDto response = chatMessageService.saveMessage(roomId, messageDto);

        // 채팅방 현재 접속 중인 사용자 목록 확인
        Set<Long> activeUsersInRoom = chatSessionService.getActiveUsersInRoom(roomId);

        // 접속 중인 사용자들에게는 자동으로 읽음 처리
        if (!activeUsersInRoom.isEmpty()) {
            // 발신자를 제외한 채팅방 접속자들에 대해 읽음 처리
            activeUsersInRoom.stream()
                    .filter(userId -> !userId.equals(messageDto.getSenderId()))
                    .forEach(userId -> chatMessageService.markMessagesAsRead(roomId, userId));
        }

        // 메시지를 특정 채팅방 구독자들에게 브로드캐스트
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, response);
    }

    /**
     * 채팅방의 메시지 목록 조회
     */
    @GetMapping("/{roomId}/message")
    public ResponseEntity<CommonResponse<List<ChatMessageResponseDto>>> getChatMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        log.info("채팅 메시지 조회: roomId={}, page={}, size={}", roomId, page, size);

        List<ChatMessageResponseDto> messages = chatMessageService.getChatMessages(roomId, page, size);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CommonResponse.success("get_messages_success", messages));
    }

    /**
     * 메시지 읽음 상태 업데이트
     */
    @PutMapping("/{roomId}/read")
    public ResponseEntity<CommonResponse<Void>> markMessagesAsRead(
            @PathVariable Long roomId) {

        // 토큰에서 userId 추출
        Long userId = SecurityUtil.getCurrentUserId();

        log.info("메시지 읽음 처리: roomId={}, userId={}", roomId, userId);

        chatMessageService.markMessagesAsRead(roomId, userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CommonResponse.success("messages_marked_as_read", null));
    }
}