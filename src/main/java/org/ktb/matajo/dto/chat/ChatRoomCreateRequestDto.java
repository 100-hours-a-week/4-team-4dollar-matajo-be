package org.ktb.matajo.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "채팅방 생성 요청 DTO")
public class ChatRoomCreateRequestDto {
    @Schema(description = "게시글 ID", example = "1", required = true)
    @NotNull(message = "게시글 ID는 필수 항목입니다")
    @Positive(message = "게시글 ID는 양수여야 합니다")
    private Long postId; // 게시글 ID
}