package org.ktb.matajo.dto.trade;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TradeInfoRequestDto {

  @NotNull private Long roomId;

  @NotBlank private String productName;

  @NotBlank private String category;

  @NotNull
  private LocalDate startDate;

  @Min(value = 1)
  private int storagePeriod;

  @Min(value = 1)
  @Max(value = 9999999)
  private int tradePrice;
}
