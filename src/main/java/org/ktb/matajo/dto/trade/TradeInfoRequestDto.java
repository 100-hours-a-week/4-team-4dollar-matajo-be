package org.ktb.matajo.dto.trade;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "거래 정보 생성 요청 DTO")
public class TradeInfoRequestDto {

    @Schema(description = "채팅방 ID", example = "1", required = true)
    @NotNull(message = "채팅방 ID는 필수 항목입니다")
    private Long roomId;

    @Schema(description = "보관 물품 이름", example = "노트북", required = true)
    @NotBlank(message = "물품 이름은 필수 항목입니다")
    private String productName;

    @Schema(description = "물품 카테고리", example = "전자기기", required = true)
    @NotBlank(message = "카테고리는 필수 항목입니다")
    private String category;

    @Schema(description = "보관 시작 날짜 (YYYY-MM-DD)", example = "2025-04-01", required = true)
    @NotNull(message = "보관 시작 날짜는 필수 항목입니다")
    private LocalDate startDate;

    @Schema(description = "보관 기간(일)", example = "30", minimum = "1", required = true)
    @Min(value = 1, message = "보관 기간은 최소 1일 이상이어야 합니다")
    private int storagePeriod;

    @Schema(description = "거래 가격(원)", example = "50000", minimum = "1", maximum = "9999999", required = true)
    @Min(value = 1, message = "거래 가격은 최소 1원 이상이어야 합니다")
    @Max(value = 9999999, message = "거래 가격은 최대 9,999,999원 이하여야 합니다")
    private int tradePrice;
}