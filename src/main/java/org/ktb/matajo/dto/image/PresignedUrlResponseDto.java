package org.ktb.matajo.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Presigned URL 응답 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "Presigned URL 응답 DTO")
public class PresignedUrlResponseDto {
    
    @Schema(description = "Presigned URL", example = "https://bucket-name.s3.amazonaws.com/...")
    private String presignedUrl;
    
    @Schema(description = "이미지 URL", example = "https://bucket-name.s3.amazonaws.com/temp/...")
    private String imageUrl;
    
    @Schema(description = "임시 키", example = "temp/20250402T083932653Z-08d54b27-81d6-46e7-bb91-teddy.png")
    private String tempKey;
}