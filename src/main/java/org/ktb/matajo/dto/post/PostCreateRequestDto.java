package org.ktb.matajo.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "게시글 생성 요청 DTO") // Swagger 문서용이니 한글 OK
public class PostCreateRequestDto {

    @Schema(description = "게시글 주소 데이터 (다음 주소 API 응답)", required = true)
    @NotNull(message = "required_post_address")
    private AddressDto postAddressData;

    @Schema(description = "게시글 제목", example = "아이패드 보관 맡겨주세요", required = true)
    @NotBlank(message = "required_post_title")
    private String postTitle;

    @Schema(description = "게시글 내용", example = "1개월 동안 아이패드 보관 맡기고 싶습니다. 소중히 보관해주실 분 찾습니다.", required = true)
    @NotBlank(message = "required_post_content")
    private String postContent;

    @Schema(description = "선호 가격 (1원 ~ 9,999,999원)", example = "30000", minimum = "1", maximum = "9999999", required = true)
    @Min(value = 1, message = "min_prefer_price")
    @Max(value = 9999999, message = "max_prefer_price")
    private int preferPrice;

    @Schema(description = "게시글 태그", example = "[\"실내\", \"가구\", \"일주일 이내\"]", required = true)
    @NotNull(message = "required_post_tags")
    private List<String> postTags;

    @Schema(description = "할인율 (0% ~ 100%)", example = "10", minimum = "0", maximum = "100", required = true)
    @Min(value = 0, message = "min_discount_rate")
    @Max(value = 100, message = "max_discount_rate")
    private int discountRate;

    @Schema(description = "숨김 상태 여부", example = "false", defaultValue = "false")
    private boolean hiddenStatus;
}
