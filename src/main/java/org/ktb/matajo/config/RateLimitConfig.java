package org.ktb.matajo.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Configuration
public class RateLimitConfig {

    // 클라이언트 ID별 버킷을 저장할 Map (메모리 기반)
    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    // API 유형별 버킷 정책 정의
    public enum ApiType {
        GENERAL,    // 일반 API
        AUTH,       // 인증 관련 API
        CHAT,       // 채팅 관련 API
        POST,       // 게시글
        LOCATION    // 메인 주소 관련 API
    }

    /**
     * 클라이언트 ID와 API 유형에 따른 버킷 반환
     * 버킷이 없으면 새로 생성하여 반환
     */
    public Bucket resolveBucket(String clientId, ApiType apiType) {
        String key = clientId + ":" + apiType.name();
        return bucketCache.computeIfAbsent(key, k -> createBucket(apiType));
    }

    /**
     * API 유형별 버킷 생성
     */
    private Bucket createBucket(ApiType apiType) {
        Bandwidth limit;

        switch (apiType) {
            case AUTH:
                // 인증 요청: 1분에 2000회
                limit = Bandwidth.classic(2000, Refill.intervally(2000, Duration.ofMinutes(1)));
                break;
            case CHAT:
                // 채팅 요청: 1분에 2000회
                limit = Bandwidth.classic(2000, Refill.intervally(2000, Duration.ofMinutes(1)));
                break;
            case POST:
                // 게시글 요청: 1분에 2000회
                limit = Bandwidth.classic(2000, Refill.intervally(2000, Duration.ofMinutes(1)));
            break;
            case LOCATION:
                // 주소 요청: 1분에 2000회
                limit = Bandwidth.classic(2000, Refill.intervally(2000, Duration.ofMinutes(1)));
                break;
            case GENERAL:
            default:
                // 일반 API 요청: 1분에 2000회
                limit = Bandwidth.classic(2000, Refill.intervally(2000, Duration.ofMinutes(1)));
                break;
        }

        return Bucket.builder().addLimit(limit).build();
    }

    public String getClientIdentifier(HttpServletRequest request) {
        // IP 주소만 사용하여 클라이언트 식별
        return "ip:" + getClientIp(request);
    }

    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");

        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("WL-Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("HTTP_CLIENT_IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        }

        // 쉼표로 구분된 여러 IP가 있을 경우 첫 번째 IP만 사용
        if (clientIp != null && clientIp.contains(",")) {
            clientIp = clientIp.split(",")[0].trim();
        }

        return clientIp;
    }

}
