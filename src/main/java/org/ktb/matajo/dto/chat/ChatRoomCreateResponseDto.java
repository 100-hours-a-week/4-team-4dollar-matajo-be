package org.ktb.matajo.dto.chat;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatRoomCreateResponseDto {
  private Long id; // 채팅 ID
}
