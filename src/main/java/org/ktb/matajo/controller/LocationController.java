package org.ktb.matajo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.location.LocationResponseDto;
import org.ktb.matajo.global.common.CommonResponse;
import org.ktb.matajo.service.post.PostService;
import org.ktb.matajo.service.location.LocationInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final PostService postService;
    private final LocationInfoService locationInfoService;

    /**
     * 특정 위치(동)의 게시글 목록 조회
     *
     * @param locationInfoId 위치 정보 ID
     * @return 위치 기반 게시글 목록
     */
    @GetMapping
    public ResponseEntity<CommonResponse<List<LocationResponseDto>>> getPostsByLocation(
            @RequestParam Long locationInfoId) {

        log.info("위치 기반 게시글 목록 조회 요청: locationInfoId={}", locationInfoId);

        List<LocationResponseDto> postList = postService.getPostsIdsByLocationInfoId(locationInfoId);

        return ResponseEntity.ok(CommonResponse.success("get_posts_by_location_success", postList));
    }

    @GetMapping("/search")
    public ResponseEntity<CommonResponse<List<String>>> searchLocations(
            @RequestParam(required = true) String dong) {
        
        log.info("위치 검색 요청: query={}", dong);
        
        List<String> searchResults = locationInfoService.searchLocations(dong);
        
        return ResponseEntity.ok(CommonResponse.success(
            "location_search_success", 
            searchResults
        ));
    }

}
