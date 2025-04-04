package org.ktb.matajo.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "게시글 상세 정보 응답 DTO")
public class PostDetailResponseDto {

    @Schema(description = "게시글 ID", example = "1")
    private Long postId;

    @Schema(description = "게시글 이미지 URL 목록", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
    private List<String> postImages;

    @Schema(description = "게시글 제목", example = "아이패드 보관 맡겨주세요")
    private String postTitle;

    @Schema(description = "게시글 태그", example = "[\"전자기기\", \"아이패드\", \"보관\"]")
    private List<String> postTags;

    @Schema(description = "선호 가격", example = "30000")
    private int preferPrice;

    @Schema(description = "게시글 내용", example = "1개월 동안 아이패드 보관 맡기고 싶습니다. 소중히 보관해주실 분 찾습니다.")
    private String postContent;

    @Schema(description = "게시글 주소", example = "서울시 강남구 삼성동")
    private String postAddress;

    @Schema(description = "작성자 닉네임", example = "타조12345")
    private String nickname;

    @Schema(description = "숨김 상태 여부", example = "false")
    private boolean hiddenStatus;

    @Schema(description = "수정 가능 여부", example = "false")
    private boolean editable;
}