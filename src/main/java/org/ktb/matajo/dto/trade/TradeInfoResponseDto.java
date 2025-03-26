package org.ktb.matajo.dto.trade;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TradeInfoResponseDto {
  private Long tradeId;
}
