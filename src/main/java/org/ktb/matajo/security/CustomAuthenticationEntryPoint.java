package org.ktb.matajo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.ktb.matajo.global.common.CommonResponse;
import org.ktb.matajo.global.error.code.ErrorCode;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, 
                        AuthenticationException authException) throws IOException, ServletException {
        
        // 401 상태 코드 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        
        // 기존 ErrorCode 사용
        ErrorCode errorCode = ErrorCode.REQUIRED_AUTHORIZATION;
        
        // 공통 응답 형식에 맞게 응답 생성
        CommonResponse<Void> errorResponse = CommonResponse.error(
                errorCode.getErrorMessage(), 
                null
        );
        
        // 응답 작성
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}