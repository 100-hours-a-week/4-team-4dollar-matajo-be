package org.ktb.matajo.dto.trade;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 사용자의 거래 내역 목록 조회 응답 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "사용자 거래 내역 목록 응답 DTO")
public class TradeInfoListResponseDto {

    @Schema(description = "거래 ID", example = "1234")
    private Long tradeId;

    @Schema(description = "보관인 여부 (true: 보관인, false: 의뢰인)", example = "true")
    private boolean keeperStatus;

    @Schema(description = "거래 상품명", example = "냉장고")
    private String productName;

    @Schema(description = "상대방 사용자 ID", example = "5678")
    private Long userId;

    @Schema(description = "상대방 사용자 닉네임", example = "타조5678")
    private String nickname;

    @Schema(description = "게시글 주소", example = "서울시 강남구")
    private String postAddress;

    @Schema(description = "거래 일자", example = "2023.09.25")
    private String tradeDate;

    @Schema(description = "보관 시작 일자", example = "2023.09.26")
    private String startDate;

    @Schema(description = "보관 기간(일)", example = "30")
    private int storagePeriod;

    @Schema(description = "거래 가격(원)", example = "5000")
    private int tradePrice;

    @Schema(description = "일당 가격(원)", example = "1000")
    private int dailyPrice;
}