package org.ktb.matajo.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "채팅방 생성 응답 DTO")
public class ChatRoomCreateResponseDto {
    @Schema(description = "생성된 채팅방 ID", example = "1")
    private Long id; // 채팅 ID
}