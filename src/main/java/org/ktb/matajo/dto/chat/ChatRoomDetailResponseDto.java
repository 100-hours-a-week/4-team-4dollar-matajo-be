package org.ktb.matajo.dto.chat;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatRoomDetailResponseDto {
    private Long roomId;
    private Long postId;
    private String postTitle;
    private String postMainImage;
    private String postAddress;
    private int preferPrice;

    // 보관인 정보
    private Long keeperId;
    private String keeperNickname;

    // 의뢰인 정보
    private Long clientId;
    private String clientNickname;
}