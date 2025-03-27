package org.ktb.matajo.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.config.RateLimitConfig;
import org.ktb.matajo.config.RateLimitConfig.ApiType;
import org.ktb.matajo.global.common.ErrorResponse;
import org.ktb.matajo.global.error.code.ErrorCode;
import org.ktb.matajo.global.error.exception.BusinessException;
import org.ktb.matajo.security.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitConfig rateLimitConfig;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // API 경로에 따라 API 유형 결정
        ApiType apiType = resolveApiType(request.getRequestURI());
        
        // 클라이언트 식별자 가져오기
        Long clientId = SecurityUtil.getCurrentUserId();
        
        // 클라이언트 ID와 API 유형에 따른 버킷 가져오기
        Bucket bucket = rateLimitConfig.resolveBucket(clientId, apiType);
        
        // 토큰 소비 시도
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        
        // Rate Limit 관련 헤더 추가
        response.addHeader("X-RateLimit-Limit", String.valueOf(getLimitForApiType(apiType)));
        response.addHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
        
        if (probe.isConsumed()) {
            //요청 허용
            return true;
        } else {
            // 요청 차단 (429 Too Many Requests)
            long waitTimeSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());
            response.addHeader("Retry-After", String.valueOf(waitTimeSeconds));
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            
            
            log.warn("Rate limit exceeded for client: {}, API type: {}, wait time: {}s", 
                    clientId, apiType, waitTimeSeconds);
            
            // 비즈니스 예외 던지기
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);

            //return false;
        }

    }
    
    /**
     * API 경로에 따른 API 유형 결정
     */
    private ApiType resolveApiType(String uri) {
        if (uri.startsWith("/auth")) {
            return ApiType.AUTH;
        } else if (uri.startsWith("/chats")) {
            return ApiType.CHAT;
        } else if (uri.contains("/locations")) {
            return ApiType.LOCATION;
        } else if (uri.contains("/posts")) {
            return ApiType.POST;
        } else {
            return ApiType.GENERAL;
        }
    }
    
    /**
     * API 유형별 제한 횟수 반환
     */
    private int getLimitForApiType(ApiType apiType) {
        switch (apiType) {
            case AUTH: return 5;
            case CHAT: return 60;
            case POST: return 50;
            case LOCATION: return 100;
            case GENERAL:
            default: return 10;
        }
    }
}