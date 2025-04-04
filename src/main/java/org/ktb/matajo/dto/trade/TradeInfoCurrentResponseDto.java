package org.ktb.matajo.dto.trade;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 지역 기반 최근 거래 정보 응답 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "지역 기반 최근 거래 내역 응답 DTO")
public class TradeInfoCurrentResponseDto {

    @Schema(description = "게시글 메인 이미지 URL", example = "https://matajo-s3.s3.ap-northeast-2.amazonaws.com/post/abc123.jpg")
    private String mainImage;      // Post의 mainImage

    @Schema(description = "거래 상품명", example = "로즈마리")
    private String productName;    // TradeInfo의 productName

    @Schema(description = "상품 카테고리", example = "식물")
    private String category;       // TradeInfo의 category

    @Schema(description = "거래 일시", example = "2023.09.25 14:30:00")
    private LocalDateTime tradeDate;  // TradeInfo의 tradeDate

    @Schema(description = "거래 가격(원)", example = "5000")
    private int tradePrice;       // TradeInfo의 tradePrice

    @Schema(description = "보관 기간(일)", example = "30")
    private int storagePeriod;    // TradeInfo의 storagePeriod
}