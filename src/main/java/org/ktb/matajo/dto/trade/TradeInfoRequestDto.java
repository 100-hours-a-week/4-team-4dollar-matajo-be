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
@Schema(description = "거래 정보 생성 요청 DTO") // Swagger 문서니까 한글 OK
public class TradeInfoRequestDto {

    @Schema(description = "채팅방 ID", example = "1", required = true)
    @NotNull(message = "Chat room ID is required.")
    private Long roomId;

    @Schema(description = "보관 물품 이름", example = "노트북", required = true)
    @NotBlank(message = "Product name is required.")
    private String productName;

    @Schema(description = "물품 카테고리", example = "전자기기", required = true)
    @NotBlank(message = "Product category is required.")
    private String category;

    @Schema(description = "보관 시작 날짜 (YYYY-MM-DD)", example = "2025-04-01", required = true)
    @NotNull(message = "Storage start date is required.")
    private LocalDate startDate;

    @Schema(description = "보관 기간(일)", example = "30", minimum = "1", required = true)
    @Min(value = 1, message = "Storage period must be at least 1 day.")
    private int storagePeriod;

    @Schema(description = "거래 가격(원)", example = "50000", minimum = "1", maximum = "9999999", required = true)
    @Min(value = 1, message = "Trade price must be at least 1 KRW.")
    @Max(value = 9999999, message = "Trade price must not exceed 9,999,999 KRW.")
    private int tradePrice;
}
