package org.ktb.matajo.dto.chat;

import lombok.*;
import org.ktb.matajo.entity.MessageType;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatMessageResponseDto {
    private Long messageId;       // 메시지 고유 ID
    private Long roomId;          // 채팅방 ID
    private Long senderId;        // 발신자 ID
    private String content;       // 메시지 내용
    private MessageType messageType;  // 메시지 타입
    private boolean readStatus;   // 읽음 상태
    private LocalDateTime createdAt;  // 생성 시간
    private String senderNickname;    // 발신자 닉네임
}
