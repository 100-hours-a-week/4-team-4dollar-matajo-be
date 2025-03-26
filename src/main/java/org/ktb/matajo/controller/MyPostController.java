package org.ktb.matajo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.post.MyPostResponseDto;
import org.ktb.matajo.global.common.CommonResponse;
import org.ktb.matajo.security.SecurityUtil;
import org.ktb.matajo.service.post.PostServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/my")
@RequiredArgsConstructor
public class MyPostController {

    private final PostServiceImpl postService;

    /**
     * 내 보관소 게시글 목록 조회
     */
    @GetMapping("/posts")
    public ResponseEntity<CommonResponse<List<MyPostResponseDto>>> getMyPosts(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {

        Long userId = SecurityUtil.getCurrentUserId();
        log.info("내 게시글 조회 요청: userId={}, offset={}, limit={}", userId, offset, limit);
        List<MyPostResponseDto> myPosts = postService.getMyPosts(userId, offset, limit);
        return ResponseEntity.ok(CommonResponse.success("get_my_posts_success", myPosts));
    }
}
