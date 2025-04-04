package org.ktb.matajo.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "채팅방 상세 정보 응답 DTO")
public class ChatRoomDetailResponseDto {
    @Schema(description = "채팅방 ID", example = "1")
    private Long roomId;

    @Schema(description = "게시글 ID", example = "100")
    private Long postId;

    @Schema(description = "게시글 제목", example = "강남역 근처 보관 가능합니다")
    private String postTitle;

    @Schema(description = "게시글 메인 이미지 URL", example = "https://example.com/images/storage.jpg")
    private String postMainImage;

    @Schema(description = "게시글 주소", example = "서울시 강남구")
    private String postAddress;

    @Schema(description = "선호 가격", example = "30000")
    private int preferPrice;

    // 보관인 정보
    @Schema(description = "보관인 ID", example = "2")
    private Long keeperId;

    @Schema(description = "보관인 닉네임", example = "보관맨")
    private String keeperNickname;

    // 의뢰인 정보
    @Schema(description = "의뢰인 ID", example = "301")
    private Long clientId;

    @Schema(description = "의뢰인 닉네임", example = "보관이필요해")
    private String clientNickname;
}