package org.ktb.matajo.dto.chat;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatRoomResponseDto {
  private Long chatRoomId;
  private boolean keeperStatus;
  private String userNickname; // 상대방 닉네임
  private String postMainImage;
  private String postAddress;
  private String lastMessage;
  private String lastMessageTime;
}
