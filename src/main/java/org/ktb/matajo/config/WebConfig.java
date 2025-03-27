package org.ktb.matajo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로에 CORS 설정 적용
                .allowedOrigins("http://localhost:3000", "https://matajo.store") // 허용할 출처(Origin) 제한
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // 허용할 HTTP 메서드 지정
                .allowedHeaders("Authorization", "Content-Type", "X-Requested-With") // 허용할 요청 헤더 지정
                .exposedHeaders("Authorization") // 클라이언트에서 접근 가능한 응답 헤더 지정
                .allowCredentials(true) // 인증 정보(쿠키, 인증 헤더) 포함 허용
                .maxAge(3600); // Preflight 요청 결과 캐시 유지 시간(초)
    }
}
