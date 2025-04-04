package org.ktb.matajo.dto.trade;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "거래 정보 생성 응답 DTO")
public class TradeInfoResponseDto {

    @Schema(description = "생성된 거래 정보 ID", example = "1")
    private Long tradeId;
}