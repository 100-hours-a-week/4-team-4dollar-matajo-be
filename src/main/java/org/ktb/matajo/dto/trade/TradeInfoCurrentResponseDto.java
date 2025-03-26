package org.ktb.matajo.dto.trade;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TradeInfoCurrentResponseDto {
    private String mainImage;      // Post의 mainImage
    private String productName;    // TradeInfo의 productName
    private String category;       // TradeInfo의 category
    private LocalDateTime tradeDate;  // TradeInfo의 tradeDate
    private int tradePrice;       // TradeInfo의 tradePrice
    private int storagePeriod;    // TradeInfo의 storagePeriod
}
