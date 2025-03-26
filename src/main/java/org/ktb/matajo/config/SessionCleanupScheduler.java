package org.ktb.matajo.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * WebSocket 세션 정리를 담당하는 스케줄러 클래스
 * WebSocketConfig와 WebSocketEventListener 간의 순환 참조를 방지하기 위해 분리된 컴포넌트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SessionCleanupScheduler {

    private final WebSocketEventListener webSocketEventListener;

    /**
     * 비활성 WebSocket 세션 정리 스케줄러
     * 5분(300,000ms)마다 실행되어 비활성 상태의 WebSocket 세션을 정리합니다.
     * 메모리 누수를 방지하고 리소스를 효율적으로 관리합니다.
     */
    @Scheduled(fixedRate = 300000)
    public void cleanupInactiveSessions() {
        log.debug("세션 정리 스케줄러 실행 시작");
        webSocketEventListener.cleanupInactiveSessions();
        log.debug("세션 정리 스케줄러 실행 완료");
    }
}