package org.ktb.matajo.service.trade;

import org.ktb.matajo.dto.chat.ChatMessageRequestDto;
import org.ktb.matajo.dto.chat.ChatMessageResponseDto;

public interface TradeService {
    ChatMessageResponseDto saveMessage(Long roomId, ChatMessageRequestDto messageDto);
}
