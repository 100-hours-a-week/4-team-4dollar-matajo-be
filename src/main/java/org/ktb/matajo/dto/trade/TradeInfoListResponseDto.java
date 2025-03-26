package org.ktb.matajo.dto.trade;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TradeInfoListResponseDto {
  private Long tradeId;
  private boolean keeperStatus;
  private String productName;
  private Long userId;
  private String postAddress;
  private String tradeDate;
  private String startDate;
  private int storagePeriod;
  private int tradePrice;
}
