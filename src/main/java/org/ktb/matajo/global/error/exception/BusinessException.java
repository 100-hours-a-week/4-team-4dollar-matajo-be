package org.ktb.matajo.global.error.exception;

import lombok.Getter;
import org.ktb.matajo.global.error.code.ErrorCode;

public class BusinessException extends RuntimeException {

    @Getter
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode){
        super(errorCode.getErrorMessage());
        this.errorCode=errorCode;
    }

}
