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
            @Valid @RequestBody TradeInfoRequestDto tradeInfoRequestDto) {

        // 로그 기록
        log.info("거래 정보 생성 요청: 상품명={}, 카테고리={}, 보관기간={}",
                tradeInfoRequestDto.getProductName(),
                tradeInfoRequestDto.getCategory(),
                tradeInfoRequestDto.getStoragePeriod());

        // 토큰에서 사용자 ID 추출 로직 (실제 구현에서는 JWT 파싱 등으로 처리)
        Long userId = 1L;

        // 거래 정보 생성
        TradeInfoResponseDto response = tradeInfoService.createTrade(tradeInfoRequestDto, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CommonResponse.success("write_trade_success", response));
    }

    @GetMapping("/my/trade")
    public ResponseEntity<CommonResponse<List<TradeInfoListResponseDto>>> getMyTrades() {

        log.info("내 거래 내역 조회 요청");

        // 토큰에서 사용자 ID 추출
        Long userId = 2L;

        // 사용자의 거래 정보 목록 조회
        List<TradeInfoListResponseDto> tradeInfoList = tradeInfoService.getMyTrades(userId);

        if (tradeInfoList.isEmpty()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CommonResponse.success("get_my_trades_success", tradeInfoList));
    }
}
