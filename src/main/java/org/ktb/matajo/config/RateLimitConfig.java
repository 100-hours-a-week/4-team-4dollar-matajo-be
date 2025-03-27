package org.ktb.matajo.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
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
    public Bucket resolveBucket(Long clientId, ApiType apiType) {
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

}