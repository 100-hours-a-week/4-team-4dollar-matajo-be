package org.ktb.matajo.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "내 게시글 정보 응답 DTO")
public class PostResponseDto {
    @Schema(description = "게시글 ID", example = "1")
    private Long postId;

    @Schema(description = "게시글 제목", example = "아이패드 보관 맡겨주세요")
    private String postTitle;

    @Schema(description = "게시글 메인 이미지 URL", example = "https://bucket-name.s3.region.amazonaws.com/post/image.jpg")
    private String postMainImage;

    @Schema(description = "게시글 주소", example = "용인시 기흥구")
    private String postAddress;

    @Schema(description = "선호 가격", example = "30000")
    private int preferPrice;

    @Schema(description = "숨김 상태 여부", example = "false")
    private boolean hiddenStatus;

    @Schema(description = "생성 일시", example = "2023.05.15 14:30:00")
    private LocalDateTime createdAt;
}