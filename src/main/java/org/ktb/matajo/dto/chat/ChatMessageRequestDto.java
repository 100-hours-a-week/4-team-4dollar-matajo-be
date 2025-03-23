package org.ktb.matajo.dto.chat;

import org.ktb.matajo.entity.MessageType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatMessageRequestDto {
  @NotNull private Long senderId;

  @NotBlank
  @Size(max = 500)
  private String content;

  @NotNull @Builder.Default private MessageType messageType = MessageType.TEXT;
}
