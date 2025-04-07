package org.ktb.matajo.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 이동된 이미지 정보 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "이동된 이미지 정보")
public class MovedImageDto {
    
    @Schema(description = "임시 키", example = "temp/20250402T083932653Z-08d54b27-81d6-46e7-bb91-teddy.png")
    private String tempKey;
    
    @Schema(description = "이미지 URL", example = "https://matajo-image.s3.amazonaws.com/post/main/20250402T085253049Z-e4a.png")
    private String imageUrl;
    
    @Schema(description = "메인 이미지 여부", example = "true")
    private boolean isMainImage;
}