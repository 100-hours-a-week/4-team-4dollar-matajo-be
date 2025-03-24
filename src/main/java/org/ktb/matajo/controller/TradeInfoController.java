package org.ktb.matajo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.trade.TradeInfoListResponseDto;
import org.ktb.matajo.dto.trade.TradeInfoRequestDto;
import org.ktb.matajo.dto.trade.TradeInfoResponseDto;
import org.ktb.matajo.global.common.CommonResponse;
import org.ktb.matajo.global.error.code.ErrorCode;
import org.ktb.matajo.global.error.exception.BusinessException;
import org.ktb.matajo.service.trade.TradeInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class TradeInfoController {
    private final TradeInfoService tradeInfoService;

    @PostMapping("/trade")
    public ResponseEntity<CommonResponse<TradeInfoResponseDto>> createTrade(
            @Valid @RequestBody TradeInfoRequestDto tradeInfoRequestDto,
            @RequestHeader(value = "userId", required = true) Long userId) {

        log.info("거래 정보 생성 요청: userId={}, 상품명={}, 카테고리={}, 보관기간={}",
                userId,
                tradeInfoRequestDto.getProductName(),
                tradeInfoRequestDto.getCategory(),
                tradeInfoRequestDto.getStoragePeriod());

        TradeInfoResponseDto response = tradeInfoService.createTrade(tradeInfoRequestDto, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CommonResponse.success("write_trade_success", response));
    }

    @GetMapping("/my/trade")
    public ResponseEntity<CommonResponse<List<TradeInfoListResponseDto>>> getMyTrades(
            @RequestHeader(value = "userId", required = true) Long userId) {

        log.info("내 거래 내역 조회 요청: userId={}", userId);

        List<TradeInfoListResponseDto> tradeInfoList = tradeInfoService.getMyTrades(userId);

        if (tradeInfoList.isEmpty()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CommonResponse.success("get_my_trades_success", tradeInfoList));
    }
}
