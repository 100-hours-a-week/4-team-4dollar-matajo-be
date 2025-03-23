package org.ktb.matajo.config;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.security.JwtUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtil;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트가 구독할 수 있는 주제 prefix
        // /topic: 일대다 메시지 (브로드캐스트)
        // /queue: 일대일 메시지 (특정 사용자 전용)
        registry.enableSimpleBroker("/topic", "/queue");

        // 클라이언트가 서버로 메시지를 보낼 때 사용할 prefix
        registry.setApplicationDestinationPrefixes("/app");

        // 특정 사용자에게 메시지를 보낼 때 사용하는 prefix
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // STOMP 엔드포인트 설정
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*") // CORS 설정 (운영 환경에서는 구체적인 도메인 지정 권장)
                .withSockJS()// SockJS 지원 (WebSocket을 지원하지 않는 브라우저를 위한 폴백)
                .setSessionCookieNeeded(true)
                .setHeartbeatTime(25000)
                .setDisconnectDelay(5000);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String token = accessor.getFirstNativeHeader("Authorization");
                    log.debug("WebSocket 연결 시도: {}", token != null ? "토큰 존재" : "토큰 없음");

                    if (token != null && token.startsWith("Bearer ")) {
                        token = token.substring(7);
                        try {
                            Claims claims = jwtUtil.parseToken(token);
                            Long userId = Long.valueOf(claims.getSubject());

                            // 세션 속성 초기화 및 사용자 ID 저장
                            if (accessor.getSessionAttributes() == null) {
                                accessor.setSessionAttributes(new HashMap<>());
                            }
                            accessor.getSessionAttributes().put("userId", userId);

                            log.info("WebSocket 인증 성공: userId={}", userId);
                        } catch (Exception e) {
                            log.error("WebSocket 인증 실패: {}", e.getMessage());
                            // 인증 실패 시 연결 거부 (선택적)
                            // return null;
                        }
                    }
                }
                return message;
            }
        });

        // 기존 스레드 풀 설정 유지
        registration.taskExecutor()
                .corePoolSize(2)
                .maxPoolSize(8)
                .queueCapacity(100);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        // 아웃바운드 채널 설정
        registration.taskExecutor()
                .corePoolSize(2)
                .maxPoolSize(8)
                .queueCapacity(100);
    }
}