package org.ktb.matajo.global.error.exception;

import org.ktb.matajo.global.error.code.ErrorCode;

public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode){
        super(errorCode.getErrorMessage());
        this.errorCode=errorCode;
    }

}
