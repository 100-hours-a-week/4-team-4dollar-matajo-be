package org.ktb.matajo.dto.post;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyPostResponseDto {
    private Long postId;
    private String postTitle;
    private String postAddress;
    private int preferPrice;
    private boolean hiddenStatus;
    private LocalDateTime createdAt;
}
