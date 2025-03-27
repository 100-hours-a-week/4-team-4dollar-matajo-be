package org.ktb.matajo.dto.location;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "위치 기반 할인 정보 응답 DTO")
public class LocationDealResponseDto {
    @Schema(description = "게시글 제목", example = "아이패드 보관 맡겨주세요")
    private String title;

    @Schema(description = "할인율 정보", example = "-15%")
    private String discount;

    @Schema(description = "이미지 URL", example = "https://example.com/image1.jpg")
    private String imageUrl;
}