package org.ktb.matajo.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.chat.ChatMessageResponseDto;
import org.ktb.matajo.entity.MessageType;
import org.ktb.matajo.global.common.ErrorResponse;
import org.ktb.matajo.global.error.code.ErrorCode;
import org.ktb.matajo.global.error.exception.BusinessException;
import org.ktb.matajo.service.chat.ChatMessageService;
import org.ktb.matajo.service.chat.ChatSessionService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    // 웹소켓 세션 ID와 구독 정보 매핑
    private final Map<String, Map<String, Long>> sessionSubscriptions = new ConcurrentHashMap<>();
    
    // 세션별 연결 오류 횟수 관리 (DoS 방지)
    private final Map<String, Integer> sessionErrorCounts = new ConcurrentHashMap<>();
    private static final int MAX_ERROR_COUNT = 5;
    
    // 마지막 활동 시간 기록 (타임아웃 감지용)
    private final Map<String, LocalDateTime> sessionLastActivity = new ConcurrentHashMap<>();
    private static final long SESSION_TIMEOUT_MINUTES = 30;

    /**
     * 웹소켓 연결 완료 이벤트 처리
     */
    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headers.getSessionId();

        try {
            // 연결 정보 초기화
            sessionSubscriptions.put(sessionId, new ConcurrentHashMap<>());
            sessionErrorCounts.remove(sessionId);
            sessionLastActivity.put(sessionId, LocalDateTime.now());

            log.debug("WebSocket 연결 완료: sessionId={}", sessionId);
        } catch (Exception e) {
            log.error("WebSocket 연결 처리 중 오류: {}", e.getMessage(), e);
        }
    }

    /**
     * 채팅방 구독 이벤트 처리
     */
    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headers.getSessionId();
        String destination = headers.getDestination();

        // 활동 시간 업데이트
        sessionLastActivity.put(sessionId, LocalDateTime.now());

        if (destination != null && destination.startsWith("/topic/chat/")) {
            try {
                // 오류 채널 구독은 무시 ("/topic/chat/{roomId}/error")
                if (destination.contains("/error")) {
                    return;
                }
                
                // "/topic/chat/{roomId}" 형식에서 roomId 추출
                String roomIdStr = destination.substring(destination.lastIndexOf('/') + 1);
                Long roomId = Long.parseLong(roomIdStr);

                // userId 파라미터 검증
                String userIdStr = headers.getFirstNativeHeader("userId");
                if (userIdStr == null || userIdStr.isEmpty()) {
                    log.warn("구독 요청 시 userId 없음: sessionId={}, destination={}", 
                            sessionId, destination);
                    incrementErrorCount(sessionId);
                    return;
                }

                Long userId = Long.parseLong(userIdStr);

                // 세션 구독 정보 저장
                if (sessionSubscriptions.containsKey(sessionId)) {
                    sessionSubscriptions.get(sessionId).put(destination, roomId);
                }

                try {
                    // 채팅방 입장 처리
                    chatSessionService.userJoinedRoom(roomId, userId);
                    
                    // 해당 사용자로 온 메시지 읽음 처리
                    chatMessageService.markMessagesAsRead(roomId, userId);

                    // 시스템 메시지 전송 (선택사항)
                    sendSystemMessage(roomId, userId, "사용자가 채팅방에 입장했습니다.");

                    log.info("채팅방 구독 완료: roomId={}, userId={}, sessionId={}",
                            roomId, userId, sessionId);
                } catch (BusinessException e) {
                    log.error("채팅방 입장 처리 중 비즈니스 예외: {}", e.getMessage());
                    sendErrorMessage(roomId, userId, e.getErrorCode().getDescription());
                } catch (Exception e) {
                    log.error("채팅방 입장 처리 중 오류: {}", e.getMessage(), e);
                    sendErrorMessage(roomId, userId, "채팅방 입장 중 오류가 발생했습니다.");
                }
            } catch (NumberFormatException e) {
                log.error("roomId 또는 userId 파싱 오류: {}", e.getMessage());
                incrementErrorCount(sessionId);
            } catch (Exception e) {
                log.error("채팅방 구독 처리 중 오류: {}", e.getMessage(), e);
                incrementErrorCount(sessionId);
            }
        }
    }

    /**
     * 채팅방 구독 해제 이벤트 처리
     */
    @EventListener
    public void handleSessionUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headers.getSessionId();
        String destination = headers.getDestination();

        // 활동 시간 업데이트
        sessionLastActivity.put(sessionId, LocalDateTime.now());

        if (destination != null && destination.startsWith("/topic/chat/")) {
            try {
                // 세션 구독 정보에서 채팅방 ID 조회
                Map<String, Long> subscriptions = sessionSubscriptions.get(sessionId);
                if (subscriptions != null && subscriptions.containsKey(destination)) {
                    Long roomId = subscriptions.get(destination);

                    // userId 헤더 확인
                    String userIdStr = headers.getFirstNativeHeader("userId");
                    if (userIdStr != null && !userIdStr.isEmpty()) {
                        Long userId = Long.parseLong(userIdStr);

                        try {
                            // 채팅방 퇴장 처리
                            chatSessionService.userLeftRoom(roomId, userId);
                            
                            // 구독 정보에서 제거
                            subscriptions.remove(destination);
                            
                            // 시스템 메시지 전송 (선택사항)
                            sendSystemMessage(roomId, userId, "사용자가 채팅방을 나갔습니다.");

                            log.info("채팅방 구독 해제: roomId={}, userId={}, sessionId={}",
                                    roomId, userId, sessionId);
                        } catch (BusinessException e) {
                            log.error("채팅방 퇴장 처리 중 비즈니스 예외: {}", e.getMessage());
                        } catch (Exception e) {
                            log.error("채팅방 퇴장 처리 중 오류: {}", e.getMessage(), e);
                        }
                    } else {
                        // userId 헤더가 없어도 구독 정보는 정리
                        subscriptions.remove(destination);
                        log.debug("userId 없이 채팅방 구독 해제: roomId={}, sessionId={}",
                                roomId, sessionId);
                    }
                }
            } catch (Exception e) {
                log.error("채팅방 구독 해제 처리 중 오류: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * 웹소켓 연결 종료 이벤트 처리
     */
    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headers.getSessionId();

        try {
            // 세션 구독 정보 조회
            Map<String, Long> subscriptions = sessionSubscriptions.remove(sessionId);
            
            if (subscriptions != null && !subscriptions.isEmpty()) {
                // 모든 구독 채팅방에서 사용자 퇴장 처리
                // userId 정보가 없어 처리 불가능하므로 로그만 남김
                log.info("WebSocket 연결 종료: sessionId={}, 구독 채팅방 수={}",
                        sessionId, subscriptions.size());
            }
            
            // 세션 관련 정보 정리
            sessionErrorCounts.remove(sessionId);
            sessionLastActivity.remove(sessionId);
        } catch (Exception e) {
            log.error("WebSocket 연결 종료 처리 중 오류: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Heartbeat 처리 메서드 (ChatMessageController에서 호출)
     */
    public void updateSessionActivity(String sessionId, Long roomId, Long userId) {
        if (sessionId == null || roomId == null || userId == null) {
            return;
        }
        
        // 활동 시간 업데이트
        sessionLastActivity.put(sessionId, LocalDateTime.now());
        
        // 세션이 해당 채팅방을 구독 중인지 확인
        Map<String, Long> subscriptions = sessionSubscriptions.get(sessionId);
        if (subscriptions != null) {
            // 해당 채팅방 구독이 없는 경우 확인
            boolean hasRoomSubscription = subscriptions.values().contains(roomId);
            
            if (!hasRoomSubscription) {
                // 구독 정보 동기화 (정리되었지만 실제로는 활성 상태인 경우)
                log.debug("Heartbeat을 통한 구독 정보 복구: roomId={}, userId={}, sessionId={}",
                        roomId, userId, sessionId);
                
                // 채팅방 세션 활성화 상태 업데이트
                try {
                    // 채팅방에 사용자가 활성화 상태인지 확인
                    Set<Long> activeUsers = chatSessionService.getActiveUsersInRoom(roomId);
                    if (!activeUsers.contains(userId)) {
                        chatSessionService.userJoinedRoom(roomId, userId);
                        log.info("Heartbeat을 통한 사용자 재활성화: roomId={}, userId={}", roomId, userId);
                    }
                } catch (Exception e) {
                    log.warn("Heartbeat 처리 중 세션 복구 오류: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 오류 카운트 증가 및 DoS 방지
     */
    private void incrementErrorCount(String sessionId) {
        if (sessionId == null) return;

        int count = sessionErrorCounts.getOrDefault(sessionId, 0) + 1;
        sessionErrorCounts.put(sessionId, count);

        if (count > MAX_ERROR_COUNT) {
            log.warn("최대 오류 횟수 초과: sessionId={}, count={}", sessionId, count);

            // 해당 세션의 구독 정보 제거
            sessionSubscriptions.remove(sessionId);

            // 일정 시간 후에 오류 카운트 리셋 (선택사항)
            scheduleErrorCountReset(sessionId);
        }
    }

    /**
     * 오류 카운트 리셋 스케줄링
     */
    private void scheduleErrorCountReset(String sessionId) {
        new Thread(() -> {
            try {
                Thread.sleep(60000); // 1분 후 리셋
                sessionErrorCounts.remove(sessionId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * 시스템 메시지 전송
     */
    private void sendSystemMessage(Long roomId, Long userId, String content) {
        try {
            ChatMessageResponseDto message = ChatMessageResponseDto.builder()
                    .messageId(-1L) // 시스템 메시지 식별용
                    .roomId(roomId)
                    .senderId(0L) // 시스템 메시지는 senderId 0 사용
                    .senderNickname("시스템")
                    .content(content)
                    .messageType(MessageType.SYSTEM)
                    .readStatus(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            messagingTemplate.convertAndSend("/topic/chat/" + roomId, message);
        } catch (Exception e) {
            log.warn("시스템 메시지 전송 중 오류: {}", e.getMessage());
        }
    }

    /**
     * 오류 메시지 전송
     */
    private void sendErrorMessage(Long roomId, Long userId, String errorMessage) {
        try {
            // 특정 사용자에게만 오류 메시지 전송
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/errors",
                    ErrorResponse.builder()
                            .code("websocket_error")
                            .message(errorMessage)
                            .build()
            );
        } catch (Exception e) {
            log.error("오류 메시지 전송 중 오류: {}", e.getMessage(), e);
        }
    }

    /**
     * 비활성 세션 정리 (스케줄러로 호출됨)
     * 실제 구현 시에는 @Scheduled 어노테이션 사용 권장
     */
    public void cleanupInactiveSessions() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(SESSION_TIMEOUT_MINUTES);

        // 타임아웃된 세션 확인
        List<String> timeoutSessions = new ArrayList<>();
        for (Map.Entry<String, LocalDateTime> entry : sessionLastActivity.entrySet()) {
            if (entry.getValue().isBefore(timeoutThreshold)) {
                timeoutSessions.add(entry.getKey());
            }
        }

        // 타임아웃된 세션 정리
        for (String sessionId : timeoutSessions) {
            try {
                Map<String, Long> subscriptions = sessionSubscriptions.remove(sessionId);
                if (subscriptions != null) {
                    log.info("비활성으로 세션 정리: sessionId={}, 구독 수={}",
                            sessionId, subscriptions.size());
                }

                sessionLastActivity.remove(sessionId);
                sessionErrorCounts.remove(sessionId);
            } catch (Exception e) {
                log.error("세션 정리 중 오류: {}", e.getMessage(), e);
            }
        }

        if (!timeoutSessions.isEmpty()) {
            log.debug("비활성 세션 정리 완료: {}개 세션", timeoutSessions.size());
        }
    }
}