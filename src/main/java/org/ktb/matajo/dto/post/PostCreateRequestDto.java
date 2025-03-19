package org.ktb.matajo.dto.post;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PostCreateRequestDto {

    private AddressDto postAddressData;  // 다음 주소 API 응답 데이터

    @NotNull
    private String postTitle;

    @NotNull
    private String postContent;

    @NotNull
    private int preferPrice;

    @NotNull
    private List<String> postTags;

    @NotNull
    private float discountRate;

    private boolean hiddenStatus;
}
