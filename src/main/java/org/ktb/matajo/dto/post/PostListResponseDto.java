package org.ktb.matajo.dto.post;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.util.List;

// dto에 관한 설명
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PostListResponseDto {

    private Long postId; //게시글 id

    private String postTitle; //게시글 제목 (한줄정리)

    private String postMainImage; // 메인이미지

    private String postAddress;

    private int preferPrice;

    private List<String> postTags;
}