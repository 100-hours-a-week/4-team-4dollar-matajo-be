package org.ktb.matajo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.location.LocationDealResponseDto;
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
    public ResponseEntity<CommonResponse<List<LocationPostResponseDto>>> getPostsByLocation(
            @RequestParam Long locationInfoId) {

        log.info("위치 기반 게시글 목록 조회 요청: locationInfoId={}", locationInfoId);

        List<LocationPostResponseDto> postList = postService.getPostsIdsByLocationInfoId(locationInfoId);

        return ResponseEntity.ok(CommonResponse.success("get_posts_by_location_success", postList));
    }


    //동 검색
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

    //지역 특가 조회
    @GetMapping("/deals")
    public ResponseEntity<CommonResponse<Map<String, Object>>> getDeals(
            @RequestParam("location_info_id") Long locationInfoId) {
      
        log.info("지역 특가 조회 요청: locationInfoId={}", locationInfoId);
      
        List<LocationDealResponseDto> deals = postService.getTopDiscountedPosts(locationInfoId);
      
        Map<String, Object> responseData = Map.of(
            "local_info_id", locationInfoId,
            "posts", deals
        );
      
        String message = deals.isEmpty() ? "no_deals_found" : "get_deals_success";
      
        return ResponseEntity.ok(CommonResponse.success(message, responseData));
    }

}
