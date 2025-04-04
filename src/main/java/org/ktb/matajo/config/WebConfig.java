//package org.ktb.matajo.config;
//
//import lombok.RequiredArgsConstructor;
////import org.ktb.matajo.interceptor.RateLimitInterceptor;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//@RequiredArgsConstructor
//
//public class WebConfig implements WebMvcConfigurer {
//
////    private final RateLimitInterceptor rateLimitInterceptor;
//
////    @Override
////    public void addInterceptors(InterceptorRegistry registry){
////        registry.addInterceptor(rateLimitInterceptor)
////            .addPathPatterns("/**") //모든 경로 적용
////            .excludePathPatterns(
////                "/swagger-ui/**", //API 문서 제외
////                "/v3/api-docs/**" // API 문서 제외
////            );
////    }
//}
