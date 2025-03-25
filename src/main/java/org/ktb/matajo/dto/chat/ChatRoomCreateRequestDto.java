package org.ktb.matajo.dto.chat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatRoomCreateRequestDto {
    @NotNull(message = "게시글 ID는 필수 항목입니다")
    @Positive(message = "게시글 ID는 양수여야 합니다")
    private Long postId; // 게시글 ID
}
