package org.ktb.matajo.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.List;

/**
 * 이미지 이동 요청 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "이미지 이동 요청 DTO")
public class ImageMoveRequestDto {
    
    @NotEmpty(message = "이동할 이미지의 임시 키는 필수입니다")
    @Schema(description = "임시 키 목록", required = true)
    private List<String> tempKeys;
    
    @NotBlank(message = "카테고리는 필수입니다")
    @Pattern(regexp = "^(post|chat)$", message = "카테고리는 'post' 또는 'chat'이어야 합니다")
    @Schema(description = "카테고리 (post 또는 chat)", example = "post", required = true)
    private String category;
    
    @Schema(description = "메인 이미지 표시 여부 목록", example = "[true, false, false]")
    private List<Boolean> mainFlags;
}