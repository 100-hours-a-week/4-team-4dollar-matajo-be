package org.ktb.matajo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.config.WebSocketEventListener;
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
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "채팅 메시지", description = "채팅 메시지 관련 API")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatSessionService chatSessionService;
    private final WebSocketEventListener webSocketEventListener;

    /**
     * 클라이언트 Heartbeat 처리 (연결 상태 확인)
     */
    @Operation(summary = "WebSocket Heartbeat", description = "WebSocket 연결 상태를 유지하기 위한 Heartbeat 메시지 (WebSocket API)")
    @MessageMapping("/heartbeat")
    public void handleHeartbeat(Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            String sessionId = headerAccessor.getSessionId();

            // 필수 파라미터 확인
            if (payload == null || !payload.containsKey("roomId") || !payload.containsKey("userId")) {
                log.warn("Heartbeat 누락된 필수 파라미터: sessionId={}", sessionId);
                return;
            }

            Long roomId = Long.valueOf(payload.get("roomId").toString());
            Long userId = Long.valueOf(payload.get("userId").toString());

            // 세션 활성 상태 업데이트
            webSocketEventListener.updateSessionActivity(sessionId, roomId, userId);

            if (log.isTraceEnabled()) {
                log.trace("Heartbeat 수신: roomId={}, userId={}, sessionId={}",
                        roomId, userId, sessionId);
            }
        } catch (Exception e) {
            // Heartbeat 오류는 심각한 문제가 아니므로 로그만 남김
            log.debug("Heartbeat 처리 중 오류: {}", e.getMessage());
        }
    }

    /**
     * WebSocket을 통한 메시지 전송
     * /app/{roomId} 엔드포인트로 메시지가 전송됨
     */
    @Operation(summary = "WebSocket 메시지 전송", description = "WebSocket을 통해 채팅 메시지를 전송합니다 (WebSocket API)")
    @MessageMapping("/{roomId}/message")
    public void sendMessage(
            @Parameter(description = "채팅방 ID", required = true)
            @DestinationVariable Long roomId,

            @Parameter(description = "메시지 내용", required = true, schema = @Schema(implementation = ChatMessageRequestDto.class))
            @Valid @Payload ChatMessageRequestDto messageDto,

            SimpMessageHeaderAccessor headerAccessor) {
        log.info("메시지 전송: roomId={}, senderId={}, type={}", roomId, messageDto.getSenderId(), messageDto.getMessageType(), messageDto.getMessageType().getValue());

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
    @Operation(summary = "채팅 메시지 목록 조회", description = "특정 채팅방의 메시지 목록을 페이징하여 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메시지 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "채팅방 없음")
    })
    @GetMapping("/{roomId}/message")
    public ResponseEntity<CommonResponse<List<ChatMessageResponseDto>>> getChatMessages(
            @Parameter(description = "채팅방 ID", required = true)
            @PathVariable Long roomId,

            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "50")
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
    @Operation(summary = "메시지 읽음 상태 업데이트", description = "특정 채팅방의 메시지를 모두 읽음 상태로 변경합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "읽음 처리 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "채팅방 없음")
    })
    @PutMapping("/{roomId}/read")
    public ResponseEntity<CommonResponse<Void>> markMessagesAsRead(
            @Parameter(description = "채팅방 ID", required = true)
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