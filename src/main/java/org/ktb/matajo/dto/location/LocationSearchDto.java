package org.ktb.matajo.dto.location;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationSearchDto {
  private String formattedAddress;  // 동 이름
}
