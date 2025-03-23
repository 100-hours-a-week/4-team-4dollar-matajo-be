package org.ktb.matajo.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.service.chat.ChatMessageService;
import org.ktb.matajo.service.chat.ChatSessionService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;

    // 웹소켓 세션 ID와 구독 정보 매핑
    private final Map<String, Map<String, Long>> sessionSubscriptions = new ConcurrentHashMap<>();

    /**
     * 웹소켓 연결 완료 이벤트 처리
     */
    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headers.getSessionId();

        // 새 세션에 대한 구독 정보 맵 초기화
        sessionSubscriptions.put(sessionId, new ConcurrentHashMap<>());

        log.debug("WebSocket 연결 완료: sessionId={}", sessionId);
    }

    /**
     * 채팅방 구독 이벤트 처리
     */
    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headers.getSessionId();
        String destination = headers.getDestination();

        if (destination != null && destination.startsWith("/topic/chat/")) {
            try {
                // "/topic/chat/123" 형식에서 "123" 추출
                String roomIdStr = destination.substring(destination.lastIndexOf('/') + 1);
                Long roomId = Long.parseLong(roomIdStr);

                // 세션 속성에서 인증된 사용자 ID 가져오기
                Map<String, Object> sessionAttributes = headers.getSessionAttributes();
                if (sessionAttributes != null && sessionAttributes.containsKey("userId")) {
                    Long userId = (Long) sessionAttributes.get("userId");

                    // 세션 구독 정보 저장
                    if (sessionSubscriptions.containsKey(sessionId)) {
                        sessionSubscriptions.get(sessionId).put(destination, roomId);
                    }

                    // 채팅방 입장 처리
                    chatSessionService.userJoinedRoom(roomId, userId);

                    // 해당 사용자 앞으로 온 모든 메시지 읽음 처리
                    chatMessageService.markMessagesAsRead(roomId, userId);

                    log.info("채팅방 구독 완료: roomId={}, userId={}, sessionId={}",
                            roomId, userId, sessionId);
                } else {
                    log.warn("인증되지 않은 사용자의 채팅방 구독 시도: sessionId={}", sessionId);
                }
            } catch (Exception e) {
                log.error("채팅방 구독 처리 중 오류: {}", e.getMessage(), e);
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

        if (destination != null && destination.startsWith("/topic/chat/")) {
            try {
                // 세션 구독 정보에서 채팅방 ID 조회
                Map<String, Long> subscriptions = sessionSubscriptions.get(sessionId);
                if (subscriptions != null && subscriptions.containsKey(destination)) {
                    Long roomId = subscriptions.get(destination);

                    // 세션 속성에서 사용자 ID 가져오기
                    Map<String, Object> sessionAttributes = headers.getSessionAttributes();
                    if (sessionAttributes != null && sessionAttributes.containsKey("userId")) {
                        Long userId = (Long) sessionAttributes.get("userId");

                        // 채팅방 퇴장 처리
                        chatSessionService.userLeftRoom(roomId, userId);

                        // 구독 정보에서 제거
                        subscriptions.remove(destination);

                        log.info("채팅방 구독 해제: roomId={}, userId={}, sessionId={}",
                                roomId, userId, sessionId);
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

        // 세션 구독 정보 조회
        Map<String, Long> subscriptions = sessionSubscriptions.remove(sessionId);
        if (subscriptions != null) {
            // 세션 속성에서 사용자 ID 가져오기
            Map<String, Object> sessionAttributes = headers.getSessionAttributes();
            if (sessionAttributes != null && sessionAttributes.containsKey("userId")) {
                Long userId = (Long) sessionAttributes.get("userId");

                // 모든 구독 중인 채팅방에서 퇴장 처리
                for (Long roomId : subscriptions.values()) {
                    chatSessionService.userLeftRoom(roomId, userId);
                    log.debug("연결 종료로 채팅방 퇴장 처리: roomId={}, userId={}", roomId, userId);
                }

                log.info("WebSocket 연결 종료: userId={}, sessionId={}, 구독 채팅방 수={}",
                        userId, sessionId, subscriptions.size());
            }
        } else {
            log.debug("WebSocket 연결 종료: sessionId={}, 구독 정보 없음", sessionId);
        }
    }
}