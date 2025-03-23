package org.ktb.matajo.service.trade;

import org.ktb.matajo.dto.trade.TradeInfoListResponseDto;
import org.ktb.matajo.dto.trade.TradeInfoRequestDto;
import org.ktb.matajo.dto.trade.TradeInfoResponseDto;

import java.util.List;

public interface TradeInfoService {
    // 거래 정보 생성
    TradeInfoResponseDto createTrade(TradeInfoRequestDto tradeInfoRequestDto, Long userId);

    // 내 거래 내역 조회
    List<TradeInfoListResponseDto> getMyTrades(Long userId);
}
