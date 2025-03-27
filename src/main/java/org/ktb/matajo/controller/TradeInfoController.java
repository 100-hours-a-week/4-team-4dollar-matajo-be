package org.ktb.matajo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.trade.TradeInfoCurrentResponseDto;
import org.ktb.matajo.dto.trade.TradeInfoListResponseDto;
import org.ktb.matajo.dto.trade.TradeInfoRequestDto;
import org.ktb.matajo.dto.trade.TradeInfoResponseDto;
import org.ktb.matajo.global.common.CommonResponse;
import org.ktb.matajo.global.error.code.ErrorCode;
import org.ktb.matajo.global.error.exception.BusinessException;
import org.ktb.matajo.security.SecurityUtil;
import org.ktb.matajo.service.trade.TradeInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "거래 정보", description = "거래 정보 관련 API")
public class TradeInfoController {
    private final TradeInfoService tradeInfoService;

    // 거래 정보 작성
    @Operation(summary = "거래 정보 생성", description = "채팅방을 기반으로 새로운 거래 정보를 생성합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "거래 정보 생성 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "채팅방 없음")
    })
    @PostMapping
    public ResponseEntity<CommonResponse<TradeInfoResponseDto>> createTrade(
            @Parameter(description = "거래 정보 생성 요청", required = true)
            @Valid @RequestBody TradeInfoRequestDto tradeInfoRequestDto) {

        Long userId = SecurityUtil.getCurrentUserId();

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

    @Operation(summary = "내 거래 내역 조회", description = "현재 로그인한 사용자의 모든 거래 내역을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "거래 내역 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류 또는 거래 내역 없음")
    })
    @GetMapping("/my-trade")
    public ResponseEntity<CommonResponse<List<TradeInfoListResponseDto>>> getMyTrades() {

        Long userId = SecurityUtil.getCurrentUserId();

        log.info("내 거래 내역 조회 요청: userId={}", userId);

        List<TradeInfoListResponseDto> tradeInfoList = tradeInfoService.getMyTrades(userId);

        if (tradeInfoList.isEmpty()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CommonResponse.success("get_my_trades_success", tradeInfoList));
    }

    // 지역 기반 최근 거래내역 2개 조회
    @Operation(summary = "지역별 최근 거래 내역 조회", description = "특정 지역의 최근 거래 내역을 최대 2개까지 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "지역별 거래 내역 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 위치 정보)"),
            @ApiResponse(responseCode = "404", description = "위치 정보 없음")
    })
    @GetMapping
    public ResponseEntity<CommonResponse<List<TradeInfoCurrentResponseDto>>> getCurrentTradeInfo(
            @Parameter(description = "위치 정보 ID", required = true, example = "1")
            @RequestParam("locationInfoId") Long locationInfoId) {

        List<TradeInfoCurrentResponseDto> currentTradeList = tradeInfoService.getCurrentTrades(locationInfoId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CommonResponse.success("get_current_trades_success", currentTradeList));
    }
}