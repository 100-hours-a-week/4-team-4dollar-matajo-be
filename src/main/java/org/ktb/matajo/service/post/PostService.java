package org.ktb.matajo.service.post;

<<<<<<< HEAD
import java.util.List;

=======
>>>>>>> feat/board
import org.ktb.matajo.dto.location.LocationResponseDto;
import org.ktb.matajo.dto.post.*;
import org.springframework.web.multipart.MultipartFile;

<<<<<<< HEAD
/** 게시글 관련 서비스 인터페이스 */
=======
import java.util.List;

/**
 * 게시글 관련 서비스 인터페이스
 */
>>>>>>> feat/board
public interface PostService {

  /**
   * 게시글 목록 조회
<<<<<<< HEAD
   *
=======
>>>>>>> feat/board
   * @param offset 시작 오프셋
   * @param limit 조회할 게시글 수
   * @return 게시글 목록 DTO
   */
  List<PostListResponseDto> getPostList(int offset, int limit);

  /**
   * 게시글 등록
<<<<<<< HEAD
   *
=======
>>>>>>> feat/board
   * @param requestDto 게시글 정보
   * @param mainImage 메인 이미지 파일
   * @param detailImages 상세 이미지 파일들
   * @return 생성된 게시글 정보
   */
<<<<<<< HEAD
  PostCreateResponseDto createPost(
      PostCreateRequestDto requestDto, MultipartFile mainImage, List<MultipartFile> detailImages);

  /**
   * 게시글 상세 조회
   *
=======
  PostCreateResponseDto createPost(PostCreateRequestDto requestDto, MultipartFile mainImage, List<MultipartFile> detailImages);

  /**
   * 게시글 상세 조회
>>>>>>> feat/board
   * @param postId 조회할 게시글 ID
   * @return 게시글 상세 정보
   */
  PostDetailResponseDto getPostDetail(Long postId);

  /**
   * 게시글 수정
<<<<<<< HEAD
   *
=======
>>>>>>> feat/board
   * @param postId 수정할 게시글 ID
   * @param requestDto 수정 정보
   * @param mainImage 새 메인 이미지
   * @param detailImages 새 상세 이미지들
   * @return 수정된 게시글 정보
   */
<<<<<<< HEAD
  PostCreateResponseDto updatePost(
      Long postId,
      PostCreateRequestDto requestDto,
      MultipartFile mainImage,
      List<MultipartFile> detailImages);

  /**
   * 게시글 삭제 (소프트 딜리트)
   *
=======
  PostCreateResponseDto updatePost(Long postId, PostCreateRequestDto requestDto,
      MultipartFile mainImage, List<MultipartFile> detailImages);

  /**
   * 게시글 삭제 (소프트 딜리트)
>>>>>>> feat/board
   * @param postId 삭제할 게시글 ID
   */
  void deletePost(Long postId);

  /**
   * 게시글 공개/비공개 상태 전환
<<<<<<< HEAD
   *
=======
>>>>>>> feat/board
   * @param postId 상태를 변경할 게시글 ID
   */
  void togglePostVisibility(Long postId);

  /**
   * 위치 ID 기반 게시글 목록 조회
<<<<<<< HEAD
   *
=======
>>>>>>> feat/board
   * @param locationInfoId 조회할 위치 정보 ID
   * @return 위치 기반 게시글 목록
   */
  List<LocationResponseDto> getPostsIdsByLocationInfoId(Long locationInfoId);
}
