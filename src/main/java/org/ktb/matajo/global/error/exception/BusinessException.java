package org.ktb.matajo.global.error.exception;

import org.ktb.matajo.global.error.code.ErrorCode;

import lombok.Getter;

public class BusinessException extends RuntimeException {

  @Getter private final ErrorCode errorCode;

  public BusinessException(ErrorCode errorCode) {
    super(errorCode.getErrorMessage());
    this.errorCode = errorCode;
  }
}
