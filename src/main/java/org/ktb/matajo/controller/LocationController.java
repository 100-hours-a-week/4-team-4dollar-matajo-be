package org.ktb.matajo.controller;

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
public class LocationController {

    private final LocationInfoService locationInfoService;

    // 동 검색
    @GetMapping("/autocomplete")
    public ResponseEntity<CommonResponse<List<String>>> searchLocations(
            @RequestParam String dong) {
        
        log.info("위치 검색 요청!: query={}", dong);
        
        List<String> searchResults = locationInfoService.searchLocations(dong);
        
        return ResponseEntity.ok(CommonResponse.success(
            "location_search_success", 
            searchResults
        ));
    }

    /**
     * 주소로 위치 정보 조회
     *
     * @param formattedAddress 형식화된 주소
     * @return 위치 정보 응답
     */
    @GetMapping("/info")
    public ResponseEntity<CommonResponse<List<LocationIdResponseDto>>> findLocationByAddress(
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
