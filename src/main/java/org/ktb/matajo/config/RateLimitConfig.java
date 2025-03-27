package org.ktb.matajo.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.ktb.matajo.security.SecurityUtil;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
                // 인증 요청: 1분에 5회
                limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)));
                break;
            case CHAT:
                // 채팅 요청: 1분에 60회
                limit = Bandwidth.classic(60, Refill.intervally(60, Duration.ofMinutes(1)));
                break;
            case POST:
                // 게시글 요청: 1분에 50회
                limit = Bandwidth.classic(50, Refill.intervally(50, Duration.ofMinutes(1)));
            break;
            case LOCATION:
                // 주소 요청: 1분에 100회
                limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
                break;
            case GENERAL:
            default:
                // 일반 API 요청: 1분에 10회
                limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)));
                break;
        }
        
        return Bucket.builder().addLimit(limit).build();
    }

    public String getClientIdentifier(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증된 사용자인 경우
        if (authentication != null && authentication.isAuthenticated()) {
            // JWT 토큰에서 파싱된 정보인 경우
            if (authentication.getPrincipal() instanceof Map) {
                Map<String, Object> principal = (Map<String, Object>) authentication.getPrincipal();
                if (principal.containsKey("nickname")) {
                    return "user:" + principal.get("nickname");
                }
            }
        }

        // 인증되지 않은 경우, IP 주소 사용
        String clientIp = getClientIp(request);
        return "ip:" + clientIp;
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