package org.ktb.matajo.dto.location;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

/** 위치 기반 게시글 정보 응답 DTO Post ID와 Address 정보 포함 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "위치 기반 게시글 정보 응답 DTO")
public class LocationPostResponseDto {
  @Schema(description = "게시글 ID", example = "1")
  private Long postId;

  @Schema(description = "주소", example = "서울시 강남구 삼성동")
  private String address;
}