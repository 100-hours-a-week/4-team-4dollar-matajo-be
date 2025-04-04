package org.ktb.matajo.dto.location;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "위치 정보 응답 DTO")
public class LocationIdResponseDto {
  @Schema(description = "위치 정보 ID", example = "1")
  private Long id;

  @Schema(description = "위도", example = "37.5665")
  private BigDecimal latitude;

  @Schema(description = "경도", example = "126.9780")
  private BigDecimal longitude;
}