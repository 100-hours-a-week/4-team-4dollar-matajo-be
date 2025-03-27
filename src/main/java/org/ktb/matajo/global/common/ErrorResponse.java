package org.ktb.matajo.global.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "API 오류 응답 모델")
public class ErrorResponse {

    @Schema(description = "오류 코드", example = "invalid_input_value")
    private String code;

    @Schema(description = "오류 메시지", example = "입력값이 유효하지 않습니다")
    private String message;

    @Schema(description = "필드별 유효성 검증 실패 정보",
            example = "{\"productName\":\"상품명은 필수 항목입니다\", \"tradePrice\":\"거래 가격은 1원 이상이어야 합니다\"}")
    private Map<String, String> validation;  // 유효성 검증 실패 시 필드별 오류
}