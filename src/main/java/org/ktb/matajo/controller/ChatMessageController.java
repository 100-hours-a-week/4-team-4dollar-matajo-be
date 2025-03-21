package org.ktb.matajo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.chat.ChatMessageRequestDto;
import org.ktb.matajo.dto.chat.ChatMessageResponseDto;
import org.ktb.matajo.entity.MessageType;
import org.ktb.matajo.global.common.CommonResponse;
import org.ktb.matajo.service.chat.ChatMessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * WebSocket을 통한 메시지 전송
     * /app/chat/{roomId} 엔드포인트로 메시지가 전송됨
     */
    @MessageMapping("/{roomId}/message")
    public void sendMessage(@DestinationVariable Long roomId, @Payload ChatMessageRequestDto messageDto) {
        log.info("메시지 전송: roomId={}, senderId={}, type={}", roomId, messageDto.getSenderId(), messageDto.getMessageType());

        // 이미지 메시지 처리를 위한 로그 추가
        if (messageDto.getMessageType() == MessageType.IMAGE) {
            log.info("이미지 메시지 처리: content(URL)={}", messageDto.getContent());
        }

        // 메시지 저장 및 처리
        ChatMessageResponseDto response = chatMessageService.saveMessage(roomId, messageDto);

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
            @PathVariable Long roomId,
            @RequestParam Long userId) {

        log.info("메시지 읽음 처리: roomId={}, userId={}", roomId, userId);

        chatMessageService.markMessagesAsRead(roomId, userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CommonResponse.success("messages_marked_as_read", null));
    }
}