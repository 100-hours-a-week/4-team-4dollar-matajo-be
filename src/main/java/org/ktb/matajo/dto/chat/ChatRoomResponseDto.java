package org.ktb.matajo.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "채팅방 목록 응답 DTO")
public class ChatRoomResponseDto {
    @Schema(description = "채팅방 ID", example = "1")
    private Long chatRoomId;

    @Schema(description = "보관인 여부 (true: 보관인, false: 의뢰인)", example = "false")
    private boolean keeperStatus;

    @Schema(description = "상대방 닉네임", example = "보관맨")
    private String userNickname;

    @Schema(description = "게시글 메인 이미지 URL", example = "https://example.com/images/storage.jpg")
    private String postMainImage;

    @Schema(description = "게시글 주소", example = "용담2동")
    private String postAddress;

    @Schema(description = "마지막 메시지 내용", example = "네, 알겠습니다.")
    private String lastMessage;

    @Schema(description = "마지막 메시지 시간", example = "2023.09.15 14:30:45")
    private String lastMessageTime;

    @Schema(description = "읽지 않은.메시지 수", example = "3")
    private Long unreadCount;
}