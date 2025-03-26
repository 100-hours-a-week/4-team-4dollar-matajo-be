package org.ktb.matajo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/**
 * WebSocket 설정 클래스
 * STOMP 프로토콜을 사용한 WebSocket 통신 구성 및 동작 방식을 정의합니다.
 */
@Configuration
@EnableWebSocketMessageBroker  // WebSocket 메시지 브로커 기능 활성화
@EnableScheduling              // 스케줄링 기능 활성화 (비활성 세션 정리 등에 사용)
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketEventListener webSocketEventListener;

    /**
     * 메시지 브로커 설정
     * 클라이언트 메시지 라우팅 경로와 서버 메시지 전송 경로를 정의합니다.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // /topic, /queue 접두사로 시작하는 목적지를 구독 가능하도록 설정
        // /topic: 일대다 메시지 브로드캐스팅(채팅방 전체 메시지)
        // /queue: 일대일 메시지 전송(개인 알림, 오류 메시지)
        registry.enableSimpleBroker("/topic", "/queue");

        // 클라이언트에서 서버로 메시지를 보낼 때 사용할 접두사 설정
        registry.setApplicationDestinationPrefixes("/app");

        // 특정 사용자에게 메시지를 보낼 때 사용하는 접두사 설정
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * STOMP 엔드포인트 등록
     * 클라이언트가 WebSocket 연결을 맺기 위한 엔드포인트를 설정합니다.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")           // WebSocket 연결 엔드포인트 URL
                .setAllowedOriginPatterns("*")     // CORS 설정 (운영 환경에서는 구체적인 도메인 지정 권장)
                .withSockJS()                       // SockJS 지원 (WebSocket을 지원하지 않는 브라우저를 위한 폴백)
                .setSessionCookieNeeded(false)      // 세션 쿠키 사용 안 함
                .setHeartbeatTime(25000)            // 하트비트 시간 설정 (ms) - 연결 유지 확인
                .setDisconnectDelay(5000);          // 연결 해제 지연 시간 (ms)
    }

    /**
     * WebSocket 전송 설정
     * 메시지 크기 및 전송 제한 사항을 정의합니다.
     */
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        // 메시지 크기 제한 설정
        registry.setMessageSizeLimit(64 * 1024);     // 클라이언트로부터 받을 수 있는 최대 메시지 크기 (64KB)
        registry.setSendBufferSizeLimit(512 * 1024); // 서버에서 전송할 수 있는 최대 버퍼 크기 (512KB)
        registry.setSendTimeLimit(20000);            // 메시지 전송 제한 시간 (20초)
    }

    /**
     * 클라이언트 인바운드 채널 설정
     * 클라이언트에서 서버로 들어오는 메시지 처리를 위한 스레드 풀 구성
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.taskExecutor()
                .corePoolSize(2)       // 기본 스레드 수
                .maxPoolSize(8)        // 최대 스레드 수
                .queueCapacity(100);   // 작업 큐 용량
    }

    /**
     * 클라이언트 아웃바운드 채널 설정
     * 서버에서 클라이언트로 나가는 메시지 처리를 위한 스레드 풀 구성
     */
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.taskExecutor()
                .corePoolSize(2)       // 기본 스레드 수
                .maxPoolSize(8)        // 최대 스레드 수
                .queueCapacity(100);   // 작업 큐 용량
    }

    /**
     * 비활성 WebSocket 세션 정리 스케줄러
     * 5분(300,000ms)마다 실행되어 비활성 상태의 WebSocket 세션을 정리합니다.
     * 메모리 누수를 방지하고 리소스를 효율적으로 관리합니다.
     */
    @Scheduled(fixedRate = 300000)
    public void cleanupInactiveSessions() {
        webSocketEventListener.cleanupInactiveSessions();
    }
}