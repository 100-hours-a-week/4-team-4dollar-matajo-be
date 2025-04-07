package org.ktb.matajo.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

/**
 * 이미지 업로드를 위한 Presigned URL 요청 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "Presigned URL 요청 DTO")
public class PresignedUrlRequestDto {
    
    @NotBlank(message = "MIME 타입은 필수입니다")
    @Pattern(regexp = "^(image/jpeg|image/jpg|image/png|image/webp|image/heic)$",
            message = "지원하지 않는 이미지 형식입니다. 지원 형식: image/jpeg, image/jpg, image/png, image/webp, image/heic")
    @Schema(description = "파일 MIME 타입", example = "image/jpeg", required = true)
    private String mimeType;
    
    @Schema(description = "파일명", example = "teddy.png")
    private String filename;
    
    @Schema(description = "카테고리 (post 또는 chat)", example = "post", required = true)
    private String category;
}