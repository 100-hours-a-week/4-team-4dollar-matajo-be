package org.ktb.matajo.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "게시글 수정 응답s DTO")
public class PostEditResponseDto {
    @Schema(description = "수정된 게시글 ID", example = "1")
    private Long postId;
} 