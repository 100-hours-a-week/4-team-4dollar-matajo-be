package org.ktb.matajo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.location.LocationDealResponseDto;
import org.ktb.matajo.dto.location.LocationIdResponseDto;
import org.ktb.matajo.dto.location.LocationPostResponseDto;
import org.ktb.matajo.global.common.CommonResponse;
import org.ktb.matajo.service.post.PostService;
import org.ktb.matajo.service.location.LocationInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Tag(name = "위치", description = "위치 정보 관련 API")
public class LocationController {

    private final LocationInfoService locationInfoService;

    @Operation(
        summary = "동 이름 자동완성 검색",
        description = "입력된 동 이름으로 위치를 검색합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "검색 성공",
            content = @Content(schema = @Schema(implementation = CommonResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(schema = @Schema(implementation = CommonResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = CommonResponse.class))
        )
    })
    @GetMapping("/autocomplete")
    public ResponseEntity<CommonResponse<List<String>>> searchLocations(
            @Parameter(description = "검색할 동 이름", required = true)
            @RequestParam String dong) {
        
        log.info("위치 검색 요청: query={}", dong);
        
        List<String> searchResults = locationInfoService.searchLocations(dong);
        
        return ResponseEntity.ok(CommonResponse.success(
            "location_search_success", 
            searchResults
        ));
    }

    @Operation(
        summary = "주소로 위치 정보 조회",
        description = "형식화된 주소로 위치 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = CommonResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 - " + ErrorCode.INVALID_POST_ADDRESS.getDescription(),
            content = @Content(schema = @Schema(implementation = CommonResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "위치 정보를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = CommonResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = CommonResponse.class))
        )
    })
    @GetMapping("/info")
    public ResponseEntity<CommonResponse<List<LocationIdResponseDto>>> findLocationByAddress(
            @Parameter(description = "형식화된 주소", required = true)
            @RequestParam("formattedAddress") String formattedAddress) {
        
        log.info("주소로 위치 정보 조회 요청: formattedAddress={}", formattedAddress);
        
        List<LocationIdResponseDto> locations = locationInfoService.findLocationByAddress(formattedAddress);
        
        if (locations.isEmpty()) {
            return ResponseEntity.status(404).body(CommonResponse.error(
                "location_not_found", Collections.emptyList()));
        }
        
        return ResponseEntity.ok(CommonResponse.success("location_find_success", locations));
    }

}
