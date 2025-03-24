package org.ktb.matajo.dto.post;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PostCreateRequestDto {

  private AddressDto postAddressData; // 다음 주소 API 응답 데이터

  @NotBlank private String postTitle;

  @NotBlank private String postContent;

  @NotNull private int preferPrice;

  @NotNull private List<String> postTags;

  @NotNull private float discountRate;

  private boolean hiddenStatus;
}
