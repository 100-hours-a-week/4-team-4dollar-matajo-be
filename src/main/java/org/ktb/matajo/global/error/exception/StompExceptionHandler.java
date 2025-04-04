package org.ktb.matajo.global.error.exception;

import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.global.common.ErrorResponse;
import org.ktb.matajo.global.error.code.ErrorCode;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class StompExceptionHandler {

    /**
     * 비즈니스 예외 처리
     */
    @MessageExceptionHandler(BusinessException.class)
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public ErrorResponse handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.error("STOMP 메시지 처리 중 비즈니스 예외 발생: code={}, message={}", 
                errorCode.getErrorMessage(), e.getMessage());
        
        return ErrorResponse.builder()
                .code(errorCode.getErrorMessage())
                .message(errorCode.getDescription())
                .build();
    }
    
    /**
     * 메시지 검증 예외 처리
     */
    @MessageExceptionHandler(org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException.class)
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public ErrorResponse handleValidationException(org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException e) {
        log.error("STOMP 메시지 검증 실패: {}", e.getMessage());
        
        return ErrorResponse.builder()
                .code(ErrorCode.INVALID_INPUT_VALUE.getErrorMessage())
                .message("메시지 형식이 올바르지 않습니다.")
                .build();
    }
    
    /**
     * 일반 예외 처리
     */
    @MessageExceptionHandler(Exception.class)
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public ErrorResponse handleException(Exception e) {
        log.error("STOMP 메시지 처리 중 예외 발생", e);
        
        return ErrorResponse.builder()
                .code(ErrorCode.INTERNAL_SERVER_ERROR.getErrorMessage())
                .message("채팅 메시지 처리 중 오류가 발생했습니다.")
                .build();
    }
}