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
import org.ktb.matajo.dto.location.LocationDealResponseDto;
import org.ktb.matajo.dto.location.LocationPostResponseDto;
import org.ktb.matajo.dto.post.*;
import org.ktb.matajo.global.common.CommonResponse;
import org.ktb.matajo.global.common.ErrorResponse;
import org.ktb.matajo.security.SecurityUtil;
import org.ktb.matajo.service.post.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Tag(name = "게시글 API", description = "게시글 CRUD 및 관련 기능을 제공하는 API")
@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(
        summary = "게시글 목록 조회",
        description = "페이지네이션을 적용하여 게시글 목록을 조회합니다. 태그 이름으로 필터링할 수 있으며, 태그는 카테고리별로 AND 조건, 카테고리 내에서는 OR 조건으로 처리됩니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 페이지네이션 파라미터", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "해당 페이지에 게시글이 없음", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<CommonResponse<List<PostListResponseDto>>> getPostList(
        @Parameter(description = "필터링할 태그 목록 (콤마로 구분)", example = "실내,전자기기,실외")
        @RequestParam(required = false) String tags,
        @Parameter(description = "조회 시작 위치", example = "0")
        @RequestParam(defaultValue = "0") int offset,
        @Parameter(description = "조회할 게시글 수", example = "10") 
        @RequestParam(defaultValue = "10") int limit) {

       log.info("게시글 목록 조회 요청: tags={}, offset={}, limit={}", tags, offset, limit);

       List<PostListResponseDto> postList;
       
       // 태그 필터링이 있는 경우
       if (tags != null && !tags.isBlank()) {
           List<String> tagNames = Arrays.asList(tags.split(","));
           // 카테고리 기반 필터링 메서드 사용
           postList = postService.getPostsByTagsWithCategoryLogic(tagNames, offset, limit);
       } else {
           // 태그 필터링이 없는 경우 기존 메서드 사용
           postList = postService.getPostList(offset, limit);
       }

        return ResponseEntity.ok(CommonResponse.success("get_posts_success", postList));
    }

    @Operation(
        summary = "게시글 등록",
        description = "새로운 게시글을 등록합니다. 메인 이미지는 필수이며, 상세 이미지는 선택사항입니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "게시글 등록 성공"),
        @ApiResponse(responseCode = "400", description = """
            - 게시글 제목이 유효하지 않음
            - 게시글 내용이 유효하지 않음
            - 게시글 주소가 유효하지 않음
            - 이미지 파일이 비어있거나 없음
            - 파일 형식이 유효하지 않음
            - 파일 크기가 제한을 초과함
            - 파일 개수가 제한을 초과함
            """, 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "게시글 작성 실패 또는 이미지 업로드 실패", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CommonResponse<PostCreateResponseDto>> createPost(
            @Valid @RequestBody PostCreateRequestDto postData) {

        Long userId = SecurityUtil.getCurrentUserId();

        PostCreateResponseDto responseDto = postService.createPost(postData, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CommonResponse.success("write_post_success", responseDto));
    }

    @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "게시글 상세 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 게시글 ID"),
        @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "게시글 상세 조회 실패")
    })
    @GetMapping("/{postId}")
    public ResponseEntity<CommonResponse<PostDetailResponseDto>> getPostDetail(
            @PathVariable Long postId) {
        
        log.info("게시글 상세 조회 요청: postId={}", postId);
        
        PostDetailResponseDto postDetail = postService.getPostDetail(postId);
        
        return ResponseEntity.ok(CommonResponse.success("get_post_detail_success", postDetail));
    }

    @Operation(
        summary = "게시글 수정",
        description = "기존 게시글의 내용을 수정합니다. 권한이 있는 사용자만 수정할 수 있습니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
        @ApiResponse(responseCode = "400", description = """
            - 게시글 ID가 유효하지 않음
            - 게시글 제목이 유효하지 않음
            - 게시글 내용이 유효하지 않음
            - 게시글 주소가 유효하지 않음
            - 이미지 관련 유효성 검증 실패
            """, 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "게시글 수정 권한 없음", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음 또는 이미 삭제된 게시글", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "게시글 수정 실패", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{postId}")
    public ResponseEntity<CommonResponse<PostEditResponseDto>> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostEditRequestDto postData){

        log.info("게시글 수정 요청: ID={}, 제목={}, 주소={}",                                                              
                postId,
                postData.getPostTitle(),
                postData.getPostAddressData());

        Long userId = SecurityUtil.getCurrentUserId();

        PostEditResponseDto responseDto = postService.updatePost(postId, postData, userId);

        return ResponseEntity.ok(CommonResponse.success("update_post_success",responseDto));
    }

    @Operation(summary = "게시글 공개 상태 변경", description = "게시글의 공개/비공개 상태를 전환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "게시글 공개 상태 변경 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "게시글 상태 변경 권한 없음"),
        @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @PatchMapping("/{postId}/visibility")
    public ResponseEntity<CommonResponse<Void>> togglePostVisibility(
            @PathVariable Long postId) {
        
        log.info("게시글 공개 상태 변경 요청: postId={}", postId);

        Long userId = SecurityUtil.getCurrentUserId();

        postService.togglePostVisibility(postId, userId);
        
        return ResponseEntity.ok(CommonResponse.success("toggle_post_visibility_success", null));
    }

    @Operation(summary = "게시글 삭제", description = "특정 게시글을 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "게시글 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "게시글 삭제 권한 없음"),
        @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없거나 이미 삭제됨"),
        @ApiResponse(responseCode = "500", description = "게시글 삭제 실패")
    })
    @DeleteMapping("/{postId}")
    public ResponseEntity<CommonResponse<Void>> deletePost(
            @PathVariable Long postId) {
        
        log.info("게시글 삭제 요청: postId={}", postId);

        Long userId = SecurityUtil.getCurrentUserId();
        
        postService.deletePost(postId, userId);
        
        return ResponseEntity.ok(CommonResponse.success("delete_post_success", null));
    }

    /**
     * ~동 기반 게시글 요청
     *
     * @param locationInfoId 위치 정보 ID
     * @return 위치 기반 게시글 목록
     */
    @Operation(summary = "위치 기반 게시글 조회", description = "특정 동네의 게시글 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "위치 기반 게시글 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 위치 정보 ID"),
        @ApiResponse(responseCode = "404", description = "해당 위치의 게시글을 찾을 수 없음")
    })
    @GetMapping("/location")
    public ResponseEntity<CommonResponse<List<LocationPostResponseDto>>> getPostsByLocation(
            @RequestParam("locationInfoId") Long locationInfoId) {

        log.info("위치 기반 게시글 목록 조회 요청: locationInfoId={}", locationInfoId);

        List<LocationPostResponseDto> postList = postService.getPostsIdsByLocationInfoId(locationInfoId);

        return ResponseEntity.ok(CommonResponse.success("get_posts_by_location_success", postList));
    }

    @Operation(
        summary = "지역 특가 조회",
        description = "특정 지역의 할인율이 높은 게시글을 조회합니다. 상위 2개의 특가 상품이 반환됩니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "지역 특가 조회 성공"),
        @ApiResponse(responseCode = "400", description = "올바르지 않은 위치 정보", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "해당 위치의 특가 정보를 찾을 수 없음", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/promotion")
    public ResponseEntity<CommonResponse<Map<String, Object>>> getDeals(
            @Parameter(description = "위치 정보 ID", required = true, example = "1") 
            @RequestParam("locationInfoId") Long locationInfoId) {

        log.info("지역 특가 조회 요청: locationInfoId={}", locationInfoId);

        List<LocationDealResponseDto> deals = postService.getTopDiscountedPosts(locationInfoId);

        Map<String, Object> responseData = Map.of(
                "locationInfoId", locationInfoId,
                "posts", deals
        );

        String message = deals.isEmpty() ? "no_deals_found" : "get_deals_success";

        return ResponseEntity.ok(CommonResponse.success(message, responseData));
    }


    /**
     * 내 보관소 게시글 목록 조회
     */
    @Operation(summary = "내 게시글 목록 조회", description = "사용자가 작성한 게시글 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "내 게시글 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 페이지네이션 파라미터"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @GetMapping("/my-posts")
    public ResponseEntity<CommonResponse<List<PostResponseDto>>> getMyPosts(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {

        Long userId = SecurityUtil.getCurrentUserId();
        log.info("내 게시글 조회 요청: userId={}, offset={}, limit={}", userId, offset, limit);
        List<PostResponseDto> myPosts = postService.getMyPosts(userId, offset, limit);
        return ResponseEntity.ok(CommonResponse.success("get_my_posts_success", myPosts));
    }

}
