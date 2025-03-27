package org.ktb.matajo.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.nio.charset.StandardCharsets;

/**
 * WebSocket 설정 클래스
 * STOMP 프로토콜을 사용한 WebSocket 통신 구성 및 동작 방식을 정의합니다.
 */
@Configuration
@EnableWebSocketMessageBroker  // WebSocket 메시지 브로커 기능 활성화
@EnableScheduling              // 스케줄링 기능 활성화 (비활성 세션 정리 등에 사용)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final ObjectMapper objectMapper;

    public WebSocketConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

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
                .setAllowedOrigins("http://localhost:3000", "https://matajo.store")
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
     * XSS 방지 인터셉터 추가
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.taskExecutor()
                .corePoolSize(2)       // 기본 스레드 수
                .maxPoolSize(8)        // 최대 스레드 수
                .queueCapacity(100);   // 작업 큐 용량

        // XSS 방지 인터셉터 추가 - JacksonConfig의 ObjectMapper 주입
        registration.interceptors(new XssProtectionChannelInterceptor(objectMapper));
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
     * 세션 정리 기능을 위한 스케줄러 빈 등록
     */
    @Bean
    public SessionCleanupScheduler sessionCleanupScheduler(WebSocketEventListener eventListener) {
        return new SessionCleanupScheduler(eventListener);
    }

    /**
     * WebSocket 메시지에 대한 XSS 보호 인터셉터
     * 정적 클래스가 아닌 일반 내부 클래스로 변경
     */
    private class XssProtectionChannelInterceptor implements ChannelInterceptor {
        private final ObjectMapper objectMapper;

        public XssProtectionChannelInterceptor(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

            if (accessor != null && StompCommand.SEND.equals(accessor.getCommand())) {
                // STOMP SEND 명령인 경우 (클라이언트→서버 메시지)
                Object payload = message.getPayload();

                if (payload instanceof byte[]) {
                    try {
                        // 메시지 페이로드 파싱
                        String messageContent = new String((byte[]) payload, StandardCharsets.UTF_8);

                        // 메시지 타입에 따라 다른 처리 적용
                        String destination = accessor.getDestination();
                        if (destination != null && destination.startsWith("/app/chat/")) {
                            // 채팅 메시지인 경우 필터링 적용
                            return sanitizeChatMessage(messageContent, accessor, message);
                        }
                    } catch (Exception e) {
                        // 예외 발생 시 로깅하고 원본 메시지 반환
                        System.err.println("메시지 처리 중 예외 발생: " + e.getMessage());
                        e.printStackTrace();
                        return message;
                    }
                }
            }

            return message;
        }

        /**
         * 채팅 메시지 내용 필터링
         */
        private Message<?> sanitizeChatMessage(String messageContent,
                                               StompHeaderAccessor accessor,
                                               Message<?> originalMessage) {
            try {
                // JSON 파싱
                JsonNode rootNode = objectMapper.readTree(messageContent);

                // messageType 확인 (TEXT, IMAGE, SYSTEM)
                String messageType = rootNode.path("messageType").asText();

                // 필터링 적용
                ObjectNode modifiedNode = (ObjectNode) rootNode;

                if ("TEXT".equals(messageType)) {
                    // 텍스트 메시지는 HTML 이스케이프 처리
                    String content = rootNode.path("content").asText();
                    String sanitized = StringEscapeUtils.escapeHtml4(content);
                    modifiedNode.put("content", sanitized);
                } else if ("IMAGE".equals(messageType)) {
                    // 이미지 메시지는 URL 검증
                    String imageUrl = rootNode.path("content").asText();
                    if (!isValidImageUrl(imageUrl)) {
                        // 유효하지 않은 URL은 빈 문자열로 대체
                        modifiedNode.put("content", "");
                    }
                }

                // 필터링된 메시지로 교체
                String sanitizedContent = objectMapper.writeValueAsString(modifiedNode);
                return MessageBuilder.createMessage(
                        sanitizedContent.getBytes(StandardCharsets.UTF_8),
                        accessor.getMessageHeaders()
                );
            } catch (Exception e) {
                // 예외 발생 시 상세 로깅 및 원본 메시지 반환
                System.err.println("채팅 메시지 필터링 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
                return originalMessage;
            }
        }

        /**
         * 이미지 URL 유효성 검증
         */
        private boolean isValidImageUrl(String url) {
            // S3 URL 등 허용된 이미지 호스트 확인
            // 프로젝트에 맞게 URL 패턴 수정 필요
            return url != null && (
                    url.startsWith("https://your-s3-bucket.s3.amazonaws.com/") ||
                            url.startsWith("https://matajo.store/api/images/")
            );
        }
    }
}