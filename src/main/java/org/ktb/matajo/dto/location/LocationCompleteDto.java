package org.ktb.matajo.dto.location;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationCompleteDto {
  private Long id;
  private String originalName;  // 동 이름
  private String cityDistrict;  // 구 이름
  private String displayAddress; // 표시용 주소 (예: "강남구 역삼동")
  
  // LocationInfo 엔티티를 DTO로 변환하는 정적 팩토리 메소드
//  public static LocationAutoCompleteDto from(LocationInfo locationInfo) {
//    return LocationAutoCompleteDto.builder()
//        .id(locationInfo.getId())
//        .originalName(locationInfo.getOriginalName())
//        .cityDistrict(locationInfo.getCityDistrict())
//        .displayAddress(locationInfo.getCityDistrict() + " " + locationInfo.getOriginalName())
//        .build();
//  }
}
