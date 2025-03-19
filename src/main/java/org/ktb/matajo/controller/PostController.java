package org.ktb.matajo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.post.PostCreateRequestDto;
import org.ktb.matajo.dto.post.PostCreateResponseDto;
import org.ktb.matajo.dto.post.PostListResponseDto;
import org.ktb.matajo.global.common.CommonResponse;
import org.ktb.matajo.service.post.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

//로깅 기능
@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    //게시글 목록 조회
    @GetMapping
    public ResponseEntity<CommonResponse<List<PostListResponseDto>>> getPostList(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {

       log.info("게시글 목록 조회 요청: offset={}, limit={}", offset, limit);

        List<PostListResponseDto> postList = postService.getPostList(offset, limit);

        return ResponseEntity.ok(CommonResponse.success("get_posts_success", postList));
    }

    /**
     * 게시글 등록 - FormData로 이미지와 데이터 함께 처리
     * @param postData 게시글 정보 (JSON 문자열)
     * @param mainImage 메인 이미지 파일
     * @param detailImages 상세 이미지 파일들 (선택적)
     * @return 게시글 생성 결과
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<PostCreateResponseDto>> createPost(
            @RequestPart("postData") PostCreateRequestDto postData,
            @RequestPart("mainImage") MultipartFile mainImage,
            @RequestPart(value = "detailImages", required = false) List<MultipartFile> detailImages) {

        log.info("게시글 등록 요청: 제목={}, 주소={}, 메인 이미지 크기={}bytes",
                postData.getPostTitle(),
                postData.getPostAddressData() != null ? postData.getPostAddressData().getAddress() : "정보 없음",
                mainImage.getSize());

        PostCreateResponseDto responseDto = postService.createPost(postData, mainImage, detailImages);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CommonResponse.success("write_post_success", responseDto));
    }
}
