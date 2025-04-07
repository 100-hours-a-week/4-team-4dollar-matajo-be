package org.ktb.matajo.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * 이미지 이동 응답 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "이미지 이동 응답 DTO")
public class ImageMoveResponseDto {
    
    @Schema(description = "이동된 이미지 목록")
    private List<MovedImageDto> movedImages;
    
    @Schema(description = "실패한 이미지 목록")
    private List<String> failedImages;
}
