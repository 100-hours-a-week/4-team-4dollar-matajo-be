package org.ktb.matajo.service.post;

import org.ktb.matajo.dto.location.LocationDealResponseDto;
import org.ktb.matajo.dto.location.LocationPostResponseDto;
import org.ktb.matajo.dto.post.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 게시글 관련 서비스 인터페이스
 */
public interface PostService {

  /**
   * 게시글 목록 조회
   * @param offset 시작 오프셋
   * @param limit 조회할 게시글 수
   * @return 게시글 목록 DTO
   */
  List<PostListResponseDto> getPostList(int offset, int limit);

  /**
   * 게시글 등록
   * @param requestDto 게시글 정보
   * @return 생성된 게시글 정보
   */
  PostCreateResponseDto createPost(PostCreateRequestDto requestDto, Long userId);

  /**
   * 게시글 상세 조회
   * @param postId 조회할 게시글 ID
   * @return 게시글 상세 정보
   */
  PostDetailResponseDto getPostDetail(Long postId);

  /**
   * 게시글 수정
   * @param postId 수정할 게시글 ID
   * @param requestDto 수정 정보
   * @return 수정된 게시글 정보
   */
  PostEditResponseDto updatePost(Long postId, PostEditRequestDto requestDto, Long userId);

  /**
   * 게시글 삭제 (소프트 딜리트)
   * @param postId 삭제할 게시글 ID
   */
  void deletePost(Long postId, Long userId);

  /**
   * 게시글 공개/비공개 상태 전환
   * @param postId 상태를 변경할 게시글 ID
   */
  void togglePostVisibility(Long postId, Long userId);

  /**
   * 위치 ID 기반 게시글 목록 조회
   * @param locationInfoId 조회할 위치 정보 ID
   * @return 위치 기반 게시글 목록
   */
  List<LocationPostResponseDto> getPostsIdsByLocationInfoId(Long locationInfoId);

  /**
   * 특정 지역의 최고 할인율 게시글 조회
   * @param locationInfoId 위치 정보 ID
   * @return 할인율 상위 2개 게시글 목록
   */
  List<LocationDealResponseDto> getTopDiscountedPosts(Long locationInfoId);
  
  // 내 보관소 조회
  List<PostResponseDto> getMyPosts(Long userId, int offset, int limit);
}
