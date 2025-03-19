package org.ktb.matajo.dto.post;

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
public class PostDetailResponseDto {
    private Long postId;
    private List<String> postImages;
    private String postTitle;
    private List<String> postTags;
    private int preferPrice;
    private String postContent;
    private String postAddress;
    private String nickname;
    private boolean hiddenStatus;
}
