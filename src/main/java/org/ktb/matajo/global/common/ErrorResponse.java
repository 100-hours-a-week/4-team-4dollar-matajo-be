package org.ktb.matajo.global.common;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
  private String code;
  private String message;
  private Map<String, String> validation; // 유효성 검증 실패 시 필드별 오류
}
