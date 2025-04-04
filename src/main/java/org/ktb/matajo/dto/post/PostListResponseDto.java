package org.ktb.matajo.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "게시글 목록 응답 DTO")
public class PostListResponseDto {

    @Schema(description = "게시글 ID", example = "1")
    private Long postId;

    @Schema(description = "게시글 제목 (한줄정리)", example = "편리한 보관 공간 제공합니다")
    private String postTitle;

    @Schema(description = "게시글 메인 이미지 URL", example = "https://bucket-name.s3.region.amazonaws.com/post/image.jpg")
    private String postMainImage;

    @Schema(description = "게시글 주소", example = "서울시 강남구")
    private String postAddress;

    @Schema(description = "선호 가격 (원)", example = "30000")
    private int preferPrice;

    @Schema(description = "게시글 태그 목록", example = "[\"실내\", \"상온보관\", \"식물\"]")
    private List<String> postTags;
}