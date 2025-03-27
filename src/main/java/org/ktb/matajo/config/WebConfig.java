package org.ktb.matajo.config;

import lombok.RequiredArgsConstructor;
import org.ktb.matajo.interceptor.RateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor

public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "https://matajo.store")  // 모든 출처 허용
                .allowedMethods("*")  // 모든 HTTP 메서드 허용
                .allowedHeaders("*")  // 모든 헤더 허용
                .allowCredentials(true)
                .maxAge(3600);        // 브라우저가 preflight 요청 결과를 캐시하는 시간(초)

    }

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(rateLimitInterceptor)
            .addPathPatterns("/**") //모든 경로 적용
            .excludePathPatterns(
                "/swagger-ui/**", //API 문서 제외
                "/v3/api-docs/**" // API 문서 제외
            );
    }
}
