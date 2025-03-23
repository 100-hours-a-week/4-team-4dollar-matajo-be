package org.ktb.matajo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/**")
        .allowedOrigins("*") // 모든 출처 허용
        .allowedMethods("*") // 모든 HTTP 메서드 허용
        .allowedHeaders("*") // 모든 헤더 허용
        .maxAge(3600); // 브라우저가 preflight 요청 결과를 캐시하는 시간(초)

    // allowCredentials(true)와 allowedOrigins("*")는 함께 사용할 수 없음
    // 보안상의 이유로 쿠키 등의 인증 정보 전송이 필요하면 특정 도메인을 지정해야 함
  }
}
