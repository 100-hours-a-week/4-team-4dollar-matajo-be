package org.ktb.matajo.dto.chat;

import jakarta.validation.constraints.NotNull;
import lombok.*;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatRoomCreateRequestDto {
    @NotNull
    private Long postId; // 게시글 ID
}
