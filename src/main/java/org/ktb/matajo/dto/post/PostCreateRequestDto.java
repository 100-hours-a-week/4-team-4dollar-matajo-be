package org.ktb.matajo.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "게시글 생성 요청 DTO")
public class PostCreateRequestDto {

    @Schema(description = "게시글 주소 데이터 (다음 주소 API 응답)", required = true)
    private AddressDto postAddressData;

    @Schema(description = "게시글 제목", example = "아이패드 보관 맡겨주세요", required = true)
    @NotBlank
    private String postTitle;

    @Schema(description = "게시글 내용", example = "1개월 동안 아이패드 보관 맡기고 싶습니다. 소중히 보관해주실 분 찾습니다.", required = true)
    @NotBlank
    private String postContent;

    @Schema(description = "선호 가격 (1원 ~ 9,999,999원)", example = "30000", minimum = "1", maximum = "9999999", required = true)
    @NotNull
    @Min(1)
    @Max(9999999)
    private int preferPrice;

    @Schema(description = "게시글 태그", example = "[\"실내\", \"가구\", \"일주일 이내\"]", required = true)
    @NotNull
    private List<String> postTags;

    @Schema(description = "할인율 (0% ~ 100%)", example = "10", minimum = "0", maximum = "100", required = true)
    @NotNull
    @Min(0)
    @Max(100)
    private int discountRate;

    @Schema(description = "숨김 상태 여부", example = "false", defaultValue = "false")
    private boolean hiddenStatus;

    @NotNull
    @Schema(description = "메인 이미지 URL 또는 경로", example = "https://example.com/images/main.jpg", required = true)
    private String mainImage;

    @Size(max = 4) // 상세 이미지 최대 4개로 제한
    @Schema(description = "상세 이미지 URL 또는 경로 목록 (최대 10개)", example = "[\"https://example.com/images/detail1.jpg\", \"https://example.com/images/detail2.jpg\"]")
    private List<String> detailImages = new ArrayList<>();
}