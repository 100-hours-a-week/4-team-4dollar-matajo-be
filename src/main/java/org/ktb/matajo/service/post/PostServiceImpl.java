package org.ktb.matajo.service.post;

<<<<<<< HEAD
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

=======
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
>>>>>>> feat/board
import org.ktb.matajo.dto.location.LocationResponseDto;
import org.ktb.matajo.dto.post.*;
import org.ktb.matajo.entity.*;
import org.ktb.matajo.global.error.code.ErrorCode;
import org.ktb.matajo.global.error.exception.BusinessException;
import org.ktb.matajo.repository.PostRepository;
import org.ktb.matajo.repository.TagRepository;
import org.ktb.matajo.repository.UserRepository;
import org.ktb.matajo.service.s3.S3Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

<<<<<<< HEAD
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
=======
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
>>>>>>> feat/board

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

<<<<<<< HEAD
  private final PostRepository postRepository;
  private final TagRepository tagRepository;
  private final AddressService addressService;
  private final S3Service s3Service;
  private final UserRepository userRepository;

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public List<PostListResponseDto> getPostList(int offset, int limit) {
    // 요청 파라미터 검증
    if (offset < 0 || limit <= 0) {
      throw new BusinessException(ErrorCode.INVALID_OFFSET_OR_LIMIT);
    }

    // 페이징 처리된 게시글 목록 조회
    Pageable pageable = PageRequest.of(offset / limit, limit);
    List<Post> posts = postRepository.findAllActivePostsOrderByCreatedAtDesc(pageable);

    // 엔티티를 DTO로 변환
    return posts.stream().map(this::convertToPostResponseDto).collect(Collectors.toList());
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public PostCreateResponseDto createPost(
      PostCreateRequestDto requestDto, MultipartFile mainImage, List<MultipartFile> detailImages) {
    // 요청 데이터 유효성 검증
    validatePostRequest(requestDto, mainImage);

    // 테스트용 하드코딩된 사용자 정보 가져오기 (kakaoId가 12345678901인 사용자)
    User testUser =
        userRepository
            .findById(1L)
            .orElseThrow(
                () -> {
                  log.error("테스트용 사용자를 찾을 수 없습니다");
                  return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

    log.info("테스트 사용자 정보: ID={}, 닉네임={}", testUser.getId(), testUser.getNickname());

    // 주소 정보 처리
    Address address;
    try {
      address = addressService.createAddressForPost(requestDto.getPostAddressData());
    } catch (Exception e) {
      log.error("주소 정보 처리 중 오류 발생: {}", e.getMessage(), e);
      throw new BusinessException(ErrorCode.INVALID_POST_ADDRESS);
    }

    // Post 엔티티 생성 및 저장
    Post post =
        Post.builder()
            .user(testUser)
            .title(requestDto.getPostTitle())
            .content(requestDto.getPostContent())
            .preferPrice(requestDto.getPreferPrice())
            .hiddenStatus(false) // 처음 등록 시 항상 false로 설정
            .discountRate(0) // 처음 등록 시 항상 0으로 설정
            .address(address)
            .build();

    Post savedPost = postRepository.save(post);

    log.info("테스트 사용자 정보: tagName={}", requestDto.getPostTags());
    // 태그 처리
    if (requestDto.getPostTags() != null && !requestDto.getPostTags().isEmpty()) {
      processPostTags(savedPost, requestDto.getPostTags());
    }

    // 이미지 처리
    processMultipartImages(savedPost, mainImage, detailImages);

    return PostCreateResponseDto.builder().postId(savedPost.getId()).build();
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public PostDetailResponseDto getPostDetail(Long postId) {
    // 게시글 조회
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(
                () -> {
                  log.error("게시글을 찾을 수 없습니다: postId={}", postId);
                  return new BusinessException(ErrorCode.NOT_FOUND_POST);
                });

    // 삭제된 게시글인지 확인
    if (post.isDeleted()) {
      log.error("이미 삭제된 게시글입니다: postId={}", postId);
      throw new BusinessException(ErrorCode.NOT_FOUND_POST);
    }

    // 이미지 URL 목록 추출 - 썸네일 이미지를 첫 번째 위치에 배치
    List<String> imageUrls = new ArrayList<>();

    // 먼저 썸네일 이미지 추가
    post.getImageList().stream()
        .filter(Image::isThumbnailStatus)
        .findFirst()
        .ifPresent(image -> imageUrls.add(image.getImageUrl()));

    // 나머지 이미지 추가 (썸네일 제외)
    post.getImageList().stream()
        .filter(image -> !image.isThumbnailStatus())
        .map(Image::getImageUrl)
        .forEach(imageUrls::add);

    // 태그 목록 추출
    List<String> tags =
        post.getPostTagList().stream()
            .map(PostTag::getTag)
            .map(Tag::getTagName)
            .collect(Collectors.toList());

    // DTO 생성 및 반환
    try {
      return PostDetailResponseDto.builder()
          .postId(post.getId())
          .postImages(imageUrls)
          .postTitle(post.getTitle())
          .postTags(tags)
          .preferPrice(post.getPreferPrice())
          .postContent(post.getContent())
          .postAddress(post.getAddress() != null ? post.getAddress().getAddress() : null)
          .nickname(post.getUser() != null ? post.getUser().getNickname() : "알 수 없음")
          .hiddenStatus(post.isHiddenStatus())
          .build();
    } catch (Exception e) {
      log.error("게시글 상세 정보 DTO 생성 중 오류 발생: {}", e.getMessage(), e);
      throw new BusinessException(ErrorCode.FAILED_TO_GET_POST_DETAIL);
    }
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public PostCreateResponseDto updatePost(
      Long postId,
      PostCreateRequestDto requestDto,
      MultipartFile mainImage,
      List<MultipartFile> detailImages) {
    // 게시글 id 검증
    if (postId == null) {
      log.error("게시글 ID가 null입니다");
      throw new BusinessException(ErrorCode.INVALID_POST_ID);
    }

    // 게시글 조회
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(
                () -> {
                  log.error("게시글을 찾을 수 없습니다: postId={}", postId);
                  return new BusinessException(ErrorCode.NOT_FOUND_POST);
                });

    // 삭제된 게시글인지 확인
    if (post.isDeleted()) {
      log.error("이미 삭제된 게시글입니다: postId={}", postId);
      throw new BusinessException(ErrorCode.NOT_FOUND_POST);
    }

    // 테스트용 하드코딩된 사용자 정보 가져오기
    User testUser =
        userRepository
            .findById(1L)
            .orElseThrow(
                () -> {
                  log.error("테스트용 사용자를 찾을 수 없습니다");
                  return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

    // 게시글 작성자와 현재 사용자가 다를 경우 권한 오류
    if (!post.getUser().getId().equals(testUser.getId())) {
      log.error("게시글 수정 권한이 없습니다: postId={}, userId={}", postId, testUser.getId());
      throw new BusinessException(ErrorCode.NO_PERMISSION_TO_UPDATE);
    }

    // 요청 데이터 유효성 검사
    validatePostRequest(requestDto, mainImage);

    try {
      // 주소 업데이트
      Address address = updatePostAddress(post, requestDto.getPostAddressData());

      // 태그 업데이트
      updatePostTags(post, requestDto.getPostTags());

      // 이미지 업데이트
      updatePostImages(post, mainImage, detailImages);

      // 할인율 계산 - 수정된 가격이 기존 가격보다 작을 경우에만
      float discountRate = requestDto.getDiscountRate();
      int currentPrice = post.getPreferPrice();
      int newPrice = requestDto.getPreferPrice();

      if (newPrice < currentPrice) {
        // 할인율 = (기존 가격 - 수정된 가격) / 기존 가격 * 100
        discountRate = (float) ((currentPrice - newPrice) / (float) currentPrice) * 100;
      }

      // 업데이트
      post.update(
          requestDto.getPostTitle(),
          requestDto.getPostContent(),
          requestDto.getPreferPrice(),
          discountRate,
          requestDto.isHiddenStatus());

      log.info("게시글 수정 완료: postId={}", postId);

      return PostCreateResponseDto.builder().postId(post.getId()).build();

    } catch (BusinessException e) {
      log.error("게시글 수정 중 비즈니스 예외 발생: {}", e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      log.error("게시글 수정 중 오류 발생: {}", e.getMessage(), e);
      throw new BusinessException(ErrorCode.FAILED_TO_UPDATE_POST);
    }
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public void deletePost(Long postId) {
    // postId null 체크 추가
    if (postId == null) {
      log.error("게시글 ID가 null입니다");
      throw new BusinessException(ErrorCode.INVALID_POST_ID);
    }

    // 게시글 조회
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(
                () -> {
                  log.error("게시글을 찾을 수 없습니다: postId={}", postId);
                  return new BusinessException(ErrorCode.NOT_FOUND_POST);
                });

    // 이미 삭제된 게시글인지 확인
    if (post.isDeleted()) {
      log.error("이미 삭제된 게시글입니다: postId={}", postId);
      throw new BusinessException(ErrorCode.NOT_FOUND_POST);
    }

    // 테스트용 하드코딩된 사용자 정보 가져오기
    User testUser =
        userRepository
            .findById(1L)
            .orElseThrow(
                () -> {
                  log.error("테스트용 사용자를 찾을 수 없습니다");
                  return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

    // 게시글 작성자와 현재 사용자가 다를 경우 권한 오류
    if (!post.getUser().getId().equals(testUser.getId())) {
      log.error("게시글 삭제 권한이 없습니다: postId={}, userId={}", postId, testUser.getId());
      throw new BusinessException(ErrorCode.NO_PERMISSION_TO_DELETE);
    }

    try {
      // 소프트 딜리트 수행
      post.delete();
      log.info("게시글 삭제 완료(소프트 딜리트): postId={}", postId);
    } catch (Exception e) {
      log.error("게시글 삭제 중 오류 발생: {}", e.getMessage(), e);
      throw new BusinessException(ErrorCode.FAILED_TO_DELETE_POST);
    }
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public void togglePostVisibility(Long postId) {
    // postId null 체크
    if (postId == null) {
      log.error("게시글 ID가 null입니다");
      throw new BusinessException(ErrorCode.INVALID_POST_ID);
    }

    // 게시글 조회
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(
                () -> {
                  log.error("게시글을 찾을 수 없습니다: postId={}", postId);
                  return new BusinessException(ErrorCode.NOT_FOUND_POST);
                });

    // 삭제된 게시글인지 확인
    if (post.isDeleted()) {
      log.error("이미 삭제된 게시글입니다: postId={}", postId);
      throw new BusinessException(ErrorCode.NOT_FOUND_POST);
    }

    // 테스트용 하드코딩된 사용자 정보 가져오기
    User testUser =
        userRepository
            .findById(1L)
            .orElseThrow(
                () -> {
                  log.error("테스트용 사용자를 찾을 수 없습니다");
                  return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

    // 게시글 작성자와 현재 사용자가 다를 경우 권한 오류
    if (!post.getUser().getId().equals(testUser.getId())) {
      log.error("게시글 상태 변경 권한이 없습니다: postId={}, userId={}", postId, testUser.getId());
      throw new BusinessException(ErrorCode.NO_PERMISSION_TO_UPDATE);
    }

    try {
      // 공개/비공개 상태 전환
      post.toggleHiddenStatus();
      log.info("게시글 공개 상태 변경 완료: postId={}, 새 상태={}", postId, post.isHiddenStatus() ? "비공개" : "공개");
    } catch (Exception e) {
      log.error("게시글 공개 상태 변경 중 오류 발생: {}", e.getMessage(), e);
      throw new BusinessException(ErrorCode.FAILED_TO_UPDATE_POST);
    }
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public List<LocationResponseDto> getPostsIdsByLocationInfoId(Long locationInfoId) {
    if (locationInfoId == null) {
      log.error("위치 정보 ID가 null입니다");
      throw new BusinessException(ErrorCode.INVALID_LOCATION_ID);
    }

    log.info("위치 ID 기반 게시글 ID 조회 시작: locationInfoId={}", locationInfoId);

    // 위치 ID로 게시글 직접 조회 (단일 쿼리 최적화)
    List<Post> posts = postRepository.findActivePostsByLocationInfoId(locationInfoId);

    if (posts.isEmpty()) {
      log.info("해당 위치에 게시글이 없습니다: locationInfoId={}", locationInfoId);
      return Collections.emptyList();
    }

    log.info("위치 ID 기반 게시글 조회 완료: locationInfoId={}, 조회된 게시글 수={}", locationInfoId, posts.size());

    // 게시글 ID와 주소 ID만 추출하여 DTO로 변환
    return posts.stream()
        .map(
            post ->
                LocationResponseDto.builder()
                    .postId(post.getId())
                    .address(post.getAddress().getAddress())
                    .build())
        .collect(Collectors.toList());
  }

  // -------------------- Private Helper Methods --------------------

  /** Post 엔티티를 DTO로 변환하는 메소드 */
  private PostListResponseDto convertToPostResponseDto(Post post) {
    // 대표 이미지 URL 가져오기 (썸네일 이미지 또는 첫 번째 이미지)
    String mainImageUrl =
        post.getImageList().stream()
            .filter(Image::isThumbnailStatus)
            .findFirst()
            .map(Image::getImageUrl)
            .orElseGet(
                () ->
                    post.getImageList().isEmpty()
                        ? null
                        : post.getImageList().get(0).getImageUrl());

    // 태그 목록 추출
    List<String> tags =
        post.getPostTagList().stream()
            .map(PostTag::getTag)
            .map(Tag::getTagName)
            .collect(Collectors.toList());

    return PostListResponseDto.builder()
        .postId(post.getId())
        .postTitle(post.getTitle())
        .postMainImage(mainImageUrl)
        .postAddress(post.getAddress().getAddress())
        .preferPrice(post.getPreferPrice())
        .postTags(tags)
        .build();
  }

  /** 게시글 요청 데이터 유효성 검증 */
  private void validatePostRequest(PostCreateRequestDto postData, MultipartFile mainImage) {
    // 제목 검증
    if (postData.getPostTitle() == null || postData.getPostTitle().isBlank()) {
      throw new BusinessException(ErrorCode.INVALID_POST_TITLE);
    }

    // 내용 검증
    if (postData.getPostContent() == null || postData.getPostContent().isBlank()) {
      throw new BusinessException(ErrorCode.INVALID_POST_CONTENT);
    }

    // 주소 데이터 검증
    if (postData.getPostAddressData() == null) {
      throw new BusinessException(ErrorCode.INVALID_POST_ADDRESS);
    }

    // 희망 가격 검증 + 0원 불가능
    if (postData.getPreferPrice() <= 0) {
      throw new BusinessException(ErrorCode.INVALID_PREFER_PRICE);
    }

    // 메인 이미지 검증
    if (mainImage == null || mainImage.isEmpty()) {
      throw new BusinessException(ErrorCode.INVALID_POST_IMAGES);
    }
  }

  /** 게시글 태그 처리 메소드 */
  private void processPostTags(Post post, List<String> tagNames) {
    // 태그 선택 x
    if (tagNames == null || tagNames.isEmpty()) {
      throw new BusinessException(ErrorCode.INVALID_POST_TAGS);
    }

    for (String tagName : tagNames) {
      Tag tag =
          tagRepository
              .findByTagName(tagName)
              .orElseThrow(
                  () -> {
                    log.error("존재하지 않는 태그: {}", tagName);
                    return new BusinessException(ErrorCode.INVALID_TAG_NAME);
                  });

      // 이미 연결된 태그인지 확인 (중복 방지)
      boolean alreadyConnected =
          post.getPostTagList().stream().anyMatch(pt -> pt.getTag().getId().equals(tag.getId()));

      if (!alreadyConnected) {
        // PostTag 연결 엔티티 생성
        PostTag postTag = PostTag.builder().post(post).tag(tag).build();

        post.getPostTagList().add(postTag);
        log.debug("태그 연결 완료: {}", tag.getTagName());
      } else {
        log.debug("이미 연결된 태그 무시: {}", tag.getTagName());
      }
    }

    log.info("게시글(ID={})에 {}개의 태그 처리 완료", post.getId(), tagNames.size());
  }

  /** 게시글 MultipartFile 이미지 처리 메소드 */
  private void processMultipartImages(
      Post post, MultipartFile mainImage, List<MultipartFile> detailImages) {
    try {
      // 메인 이미지 처리 (썸네일로 설정)
      if (mainImage != null && !mainImage.isEmpty()) {
        String mainImageUrl = s3Service.uploadImage(mainImage, "post");

        Image thumbnailImage =
            Image.builder()
                .post(post)
                .imageUrl(mainImageUrl)
                .thumbnailStatus(true) // 메인 이미지는 썸네일로 설정
                .build();

        post.getImageList().add(thumbnailImage);
        log.info("메인 이미지(썸네일) 처리 완료: {}", mainImageUrl);
      }

      // 상세 이미지 처리
      if (detailImages != null && !detailImages.isEmpty()) {
        List<String> imageUrls = s3Service.uploadImages(detailImages);

        for (String imageUrl : imageUrls) {
          Image image =
              Image.builder()
                  .post(post)
                  .imageUrl(imageUrl)
                  .thumbnailStatus(false) // 상세 이미지는 썸네일 아님
                  .build();

          post.getImageList().add(image);
        }
        log.info("{}개의 상세 이미지 처리 완료", detailImages.size());
      }
    } catch (BusinessException e) {
      log.error("이미지 처리 중 비즈니스 예외 발생: {}", e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      log.error("이미지 처리 중 오류 발생: {}", e.getMessage(), e);
      throw new BusinessException(ErrorCode.FAILED_TO_WRITE_POST);
    }
  }

  /** 게시글 주소 업데이트 */
  private Address updatePostAddress(Post post, AddressDto addressDto) {
    try {
      // 1. 기존 주소 존재 확인
      Address currentAddress = post.getAddress();
      if (currentAddress == null) {
        // 기존 주소가 없는 경우 (드문 경우) 새로 생성
        log.info("게시글에 기존 주소 정보가 없어 새로 생성합니다: postId={}", post.getId());
        Address newAddress = addressService.createAddressForPost(addressDto);
        post.updateAddress(newAddress);
        return newAddress;
      }

      // 2. 주소 변경 여부 확인 (최적화 - 불필요한 업데이트 방지)
      if (currentAddress.getAddress().equals(addressDto.getAddress())
          && currentAddress.getZonecode().equals(addressDto.getZonecode())) {
        log.info("주소 정보가 동일하여 업데이트 생략: postId={}", post.getId());
        return currentAddress;
      }

      // 3. 기존 주소 엔티티 직접 업데이트
      log.info(
          "게시글 주소 업데이트 시작: postId={}, 기존 주소={}, 새 주소={}",
          post.getId(),
          currentAddress.getAddress(),
          addressDto.getAddress());

      return addressService.updateAddress(currentAddress, addressDto);

    } catch (Exception e) {
      log.error("게시글 주소 업데이트 중 오류 발생: {}", e.getMessage(), e);
      throw new BusinessException(ErrorCode.FAILED_TO_UPDATE_POST);
    }
  }

  /** 게시글 태그 업데이트 */
  private void updatePostTags(Post post, List<String> newTagNames) {
    // 기존 태그 연결 정보 모두 제거
    post.getPostTagList().clear();

    // 새로운 태그 연결 정보 추가
    for (String tagName : newTagNames) {
      Tag tag =
          tagRepository
              .findByTagName(tagName)
              .orElseThrow(
                  () -> {
                    log.error("존재하지 않는 태그: {}", tagName);
                    return new BusinessException(ErrorCode.INVALID_TAG_NAME);
                  });

      PostTag postTag = PostTag.builder().post(post).tag(tag).build();

      post.getPostTagList().add(postTag);
    }

    log.debug("게시글 태그 업데이트 완료: postId={}, tags={}", post.getId(), newTagNames);
  }

  /** 게시글 이미지 업데이트 */
  private void updatePostImages(
      Post post, MultipartFile newMainImage, List<MultipartFile> newDetailImages) {
    // 새 이미지가 없으면 아무 작업 안 함
    boolean hasNewMainImage = newMainImage != null && !newMainImage.isEmpty();
    boolean hasNewDetailImages = newDetailImages != null && !newDetailImages.isEmpty();

    if (!hasNewMainImage && !hasNewDetailImages) {
      log.debug("새 이미지가 없어 이미지 업데이트를 건너뜁니다: postId={}", post.getId());
      return;
    }

    try {
      // 메인 이미지 업데이트 (새 이미지가 있는 경우만)
      if (hasNewMainImage) {
        // 기존 메인 이미지(썸네일) 찾아서 삭제
        Image oldMainImage = null;
        for (Image image : post.getImageList()) {
          if (image.isThumbnailStatus()) {
            oldMainImage = image;
            break;
          }
        }

        if (oldMainImage != null) {
          // S3에서 기존 이미지 삭제
          s3Service.deleteImage(oldMainImage.getImageUrl());
          // 이미지 목록에서 제거
          post.getImageList().remove(oldMainImage);
        }

        // 새 메인 이미지 업로드
        String newImageUrl = s3Service.uploadImage(newMainImage, "post");

        Image thumbnailImage =
            Image.builder().post(post).imageUrl(newImageUrl).thumbnailStatus(true).build();

        post.getImageList().add(thumbnailImage);
        log.debug("새 메인 이미지 업로드 완료: {}", newImageUrl);
      }

      // 상세 이미지 업데이트 (새 이미지가 있는 경우만)
      if (hasNewDetailImages) {
        // 기존 상세 이미지들 찾아서 삭제
        List<Image> oldDetailImages = new ArrayList<>();
        for (Image image : post.getImageList()) {
          if (!image.isThumbnailStatus()) {
            oldDetailImages.add(image);
          }
        }

        // S3에서 기존 상세 이미지들 삭제
        for (Image oldImage : oldDetailImages) {
          s3Service.deleteImage(oldImage.getImageUrl());
          post.getImageList().remove(oldImage);
        }

        // 새 상세 이미지 업로드
        List<String> newImageUrls = s3Service.uploadImages(newDetailImages);

        for (String imageUrl : newImageUrls) {
          Image detailImage =
              Image.builder().post(post).imageUrl(imageUrl).thumbnailStatus(false).build();

          post.getImageList().add(detailImage);
        }
        log.debug("{}개의 새 상세 이미지 업로드 완료", newDetailImages.size());
      }

      log.info(
          "게시글 이미지 업데이트 완료: postId={}, 메인 이미지={}, 상세 이미지={}개",
          post.getId(),
          hasNewMainImage,
          hasNewDetailImages ? newDetailImages.size() : 0);
    } catch (BusinessException e) {
      log.error("이미지 업데이트 중 비즈니스 예외 발생: {}", e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      log.error("이미지 업데이트 중 오류 발생: {}", e.getMessage(), e);
      throw new BusinessException(ErrorCode.FAILED_TO_UPDATE_POST);
    }
  }
}
=======
    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    //    private final UserService userService;
    private final AddressService addressService;
    private final S3Service s3Service;

    private final UserRepository userRepository;

    /**
     * 게시글 목록 조회 메소드
     */
    @Transactional(readOnly = true)
    public List<PostListResponseDto> getPostList(int offset, int limit) {
        // 요청 파라미터 검증
        if (offset < 0 || limit <= 0) {
            throw new BusinessException(ErrorCode.INVALID_OFFSET_OR_LIMIT);
        }

        // 페이징 처리된 게시글 목록 조회
        Pageable pageable = PageRequest.of(offset / limit, limit);
        List<Post> posts = postRepository.findAllActivePostsOrderByCreatedAtDesc(pageable);

        // if (posts.isEmpty()) {
        //     throw new BusinessException(ErrorCode.NOT_FOUND_POSTS_PAGE);
        // }

        // 엔티티를 DTO로 변환
        return posts.stream()
                .map(this::convertToPostResponseDto)
                .collect(Collectors.toList());
    }

    private PostListResponseDto convertToPostResponseDto(Post post) {
        // 대표 이미지 URL 가져오기 (썸네일 이미지 또는 첫 번째 이미지)
        String mainImageUrl = post.getImageList().stream()
                .filter(Image::isThumbnailStatus)
                .findFirst()
                .map(Image::getImageUrl)
                .orElseGet(() -> post.getImageList().isEmpty() ? null : post.getImageList().get(0).getImageUrl());

        // 태그 목록 추출
        List<String> tags = post.getPostTagList().stream()
                .map(PostTag::getTag)
                .map(tag -> tag.getTagName())
                .collect(Collectors.toList());

        return PostListResponseDto.builder()
                .postId(post.getId())
                .postTitle(post.getTitle())
                .postMainImage(mainImageUrl)
                .postAddress(post.getAddress().getAddress())
                .preferPrice(post.getPreferPrice())
                .postTags(tags)
                .build();
    }


    /**
     * 게시글 등록 메소드 - MultipartFile로 이미지 처리
     * @param requestDto 게시글 정보
     * @param mainImage 메인 이미지 파일
     * @param detailImages 상세 이미지 파일들
     * @return 생성된 게시글 ID를 담은 응답 DTO
     */
    @Transactional
    public PostCreateResponseDto createPost(PostCreateRequestDto requestDto, MultipartFile mainImage, List<MultipartFile> detailImages) {
        // 요청 데이터 유효성 검증
        validatePostRequest(requestDto, mainImage);

        // 현재 인증된 사용자 정보 가져오기
//        User currentUser = userService.getCurrentUser();

        // 테스트용 하드코딩된 사용자 정보 가져오기 (kakaoId가 12345678901인 사용자)
        User testUser = userRepository.findById(1L)
                .orElseThrow(() -> {
                    log.error("테스트용 사용자를 찾을 수 없습니다");
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        log.info("테스트 사용자 정보: ID={}, 닉네임={}", testUser.getId(), testUser.getNickname());

        //TODO:keeper인 사람만 게시글을 등록할 수 있다.

        // 주소 정보 처리
        Address address;
        try {
            address = addressService.createAddressForPost(requestDto.getPostAddressData());
        } catch (Exception e) {
            log.error("주소 정보 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INVALID_POST_ADDRESS);
        }

        // Post 엔티티 생성 및 저장
        Post post = Post.builder()
                .user(testUser)
                .title(requestDto.getPostTitle())
                .content(requestDto.getPostContent())
                .preferPrice(requestDto.getPreferPrice())
                .hiddenStatus(false) // 처음 등록 시 항상 false로 설정
                .discountRate(0) // 처음 등록 시 항상 0으로 설정
                .address(address)
                .build();

        Post savedPost = postRepository.save(post);

        log.info("테스트 사용자 정보: tagName={}", requestDto.getPostTags());
        // 태그 처리
        if (requestDto.getPostTags() != null && !requestDto.getPostTags().isEmpty()) {
            processPostTags(savedPost, requestDto.getPostTags());
        }

        // 이미지 처리
        processMultipartImages(savedPost, mainImage, detailImages);

        return PostCreateResponseDto.builder()
                .postId(savedPost.getId())
                .build();
    }

    /**
     * 게시글 요청 데이터 유효성 검증 (MultipartFile 버전)
     */
    private void validatePostRequest(PostCreateRequestDto postData, MultipartFile mainImage) {
        // 제목 검증
        if (postData.getPostTitle() == null || postData.getPostTitle().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_POST_TITLE);
        }

        // 내용 검증
        if (postData.getPostContent() == null || postData.getPostContent().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_POST_CONTENT);
        }

        // 주소 데이터 검증
        if (postData.getPostAddressData() == null) {
            throw new BusinessException(ErrorCode.INVALID_POST_ADDRESS);
        }

        // 희망 가격 검증 + 0원 불가능
        if (postData.getPreferPrice() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PREFER_PRICE);
        }

        // 메인 이미지 검증
        if (mainImage == null || mainImage.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_POST_IMAGES);
        }
    }

    /**
     * 게시글 태그 처리 메소드
     */
    private void processPostTags(Post post, List<String> tagNames) {
        // 태그 선택 x
        if (tagNames == null || tagNames.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_POST_TAGS);
        }

        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByTagName(tagName)
                    .orElseThrow(() -> {
                        log.error("존재하지 않는 태그: {}", tagName);
                        return new BusinessException(ErrorCode.TAG_NAME_NOT_FOUND);
                    });

            // 이미 연결된 태그인지 확인 (중복 방지)
            boolean alreadyConnected = post.getPostTagList().stream()
                    .anyMatch(pt -> pt.getTag().getId().equals(tag.getId()));

            if (!alreadyConnected) {
                // PostTag 연결 엔티티 생성
                PostTag postTag = PostTag.builder()
                        .post(post)
                        .tag(tag)
                        .build();

                post.getPostTagList().add(postTag);
                log.debug("태그 연결 완료: {}", tag.getTagName());
            } else {
                log.debug("이미 연결된 태그 무시: {}", tag.getTagName());
            }
        }

        log.info("게시글(ID={})에 {}개의 태그 처리 완료", post.getId(), tagNames.size());
    }

    /**
     * 게시글 MultipartFile 이미지 처리 메소드
     */
    private void processMultipartImages(Post post, MultipartFile mainImage, List<MultipartFile> detailImages) {
        try {
            // 메인 이미지 처리 (썸네일로 설정)
            if (mainImage != null && !mainImage.isEmpty()) {
                String mainImageUrl = s3Service.uploadImage(mainImage, "post");

                Image thumbnailImage = Image.builder()
                        .post(post)
                        .imageUrl(mainImageUrl)
                        .thumbnailStatus(true)  // 메인 이미지는 썸네일로 설정
                        .build();

                post.getImageList().add(thumbnailImage);
                log.info("메인 이미지(썸네일) 처리 완료: {}", mainImageUrl);
            }

            // 상세 이미지 처리
            if (detailImages != null && !detailImages.isEmpty()) {
                List<String> imageUrls = s3Service.uploadImages(detailImages);

                for (String imageUrl : imageUrls) {
                    Image image = Image.builder()
                            .post(post)
                            .imageUrl(imageUrl)
                            .thumbnailStatus(false)  // 상세 이미지는 썸네일 아님
                            .build();

                    post.getImageList().add(image);
                }
                log.info("{}개의 상세 이미지 처리 완료", detailImages.size());
            }
        } catch (BusinessException e) {
            log.error("이미지 처리 중 비즈니스 예외 발생: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("이미지 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FAILED_TO_WRITE_POST);
        }
    }

    /**
     * 게시글 상세 조회 메소드
     * @param postId 조회할 게시글 ID
     * @return 게시글 상세 정보 DTO
     */
    @Transactional(readOnly = true)
    public PostDetailResponseDto getPostDetail(Long postId) {
        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("게시글을 찾을 수 없습니다: postId={}", postId);
                    return new BusinessException(ErrorCode.POST_NOT_FOUND);
                });

        // 삭제된 게시글인지 확인
        if (post.isDeleted()) {
            log.error("이미 삭제된 게시글입니다: postId={}", postId);
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        // 이미지 URL 목록 추출 - 썸네일 이미지를 첫 번째 위치에 배치
        List<String> imageUrls = new ArrayList<>();

        // 먼저 썸네일 이미지 추가
        post.getImageList().stream()
                .filter(Image::isThumbnailStatus)
                .findFirst()
                .ifPresent(image -> imageUrls.add(image.getImageUrl()));

        // 나머지 이미지 추가 (썸네일 제외)
        post.getImageList().stream()
                .filter(image -> !image.isThumbnailStatus())
                .map(Image::getImageUrl)
                .forEach(imageUrls::add);

        // 태그 목록 추출
        List<String> tags = post.getPostTagList().stream()
                .map(PostTag::getTag)
                .map(Tag::getTagName)
                .collect(Collectors.toList());

        // DTO 생성 및 반환
        try {
            // DTO 생성 및 반환
            return PostDetailResponseDto.builder()
                    .postId(post.getId())
                    .postImages(imageUrls)
                    .postTitle(post.getTitle())
                    .postTags(tags)
                    .preferPrice(post.getPreferPrice())
                    .postContent(post.getContent())
                    .postAddress(post.getAddress() != null ? post.getAddress().getAddress() : null)
                    .nickname(post.getUser() != null ? post.getUser().getNickname() : "알 수 없음")
                    .hiddenStatus(post.isHiddenStatus())
                    .build();
        } catch (Exception e) {
            log.error("게시글 상세 정보 DTO 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FAILED_TO_GET_POST_DETAIL);
        }
    }



    /**
     * 게시글 수정 메서드
     * @param postId 수정할 게시글 ID
     * @param requestDto 수정 정보 DTO
     * @param mainImage 새 메인 이미지 (선택적)
     * @param detailImages 새 상세 이미지들 (선택적)
     * @return 수정된 게시글 ID 응답 DTO
     */
    @Transactional
    public PostCreateResponseDto updatePost(Long postId,PostCreateRequestDto requestDto,
                                            MultipartFile mainImage,List<MultipartFile> detailImages){

        //게시글 id 검증
        if (postId == null) {
            log.error("게시글 ID가 null입니다");
            throw new BusinessException(ErrorCode.INVALID_POST_ID);
        }

        //게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("게시글을 찾을 수 없습니다: postId={}", postId);
                    return new BusinessException(ErrorCode.POST_NOT_FOUND);
                });

        //삭제된 게시글인지 확인
        if (post.isDeleted()) {
            log.error("이미 삭제된 게시글입니다: postId={}", postId);
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        // 현재 인증된 사용자 정보 가져오기
        // User currentUser = userService.getCurrentUser();

        // 테스트용 하드코딩된 사용자 정보 가져오기
        User testUser = userRepository.findById(1L)
                .orElseThrow(() -> {
                    log.error("테스트용 사용자를 찾을 수 없습니다");
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);});

        //게시글 작성자- 현재 사용자 비교
        // 게시글 작성자와 현재 사용자가 다를 경우 권한 오류
        if (!post.getUser().getId().equals(testUser.getId())) {
            log.error("게시글 수정 권한이 없습니다: postId={}, userId={}", postId, testUser.getId());
            throw new BusinessException(ErrorCode.NO_PERMISSION_TO_UPDATE);
        }

        //요청 데이터 유효성 검사
        validatePostRequest(requestDto,mainImage);


        try{
            // 주소 업데이트
            Address address = updatePostAddress(post, requestDto.getPostAddressData());

            // 태그 업데이트
            updatePostTags(post, requestDto.getPostTags());

            // 이미지 업데이트
            updatePostImages(post, mainImage, detailImages);


            // 할인율 계산 - 수정된 가격이 기존 가격보다 작을 경우에만
            float discountRate = requestDto.getDiscountRate();
            int currentPrice = post.getPreferPrice();
            int newPrice = requestDto.getPreferPrice();
        
            if (newPrice < currentPrice) {
                // 할인율 = (기존 가격 - 수정된 가격) / 기존 가격 * 100
                discountRate = (float) ((currentPrice - newPrice) / (float) currentPrice) * 100;
            }


            //업데이트
            post.update(
                    requestDto.getPostTitle(),
                    requestDto.getPostContent(),
                    requestDto.getPreferPrice(),
                    discountRate,
                    requestDto.isHiddenStatus()
            );

            log.info("게시글 수정 완료: postId={}", postId);

            return PostCreateResponseDto.builder()
                    .postId(post.getId())
                    .build();

        } catch (BusinessException e){
            log.error("게시글 수정 중 비즈니스 예외 발생: {}", e.getMessage(), e);
            throw e;
        } catch(Exception e){
            log.error("게시글 수정 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FAILED_TO_UPDATE_POST);

        }
    }

    /**
     * 게시글 주소 업데이트
     */
    private Address updatePostAddress(Post post, AddressDto addressDto) {
        try {
            // 1. 기존 주소 존재 확인
            Address currentAddress = post.getAddress();
            if (currentAddress == null) {
                // 기존 주소가 없는 경우 (드문 경우) 새로 생성
                log.info("게시글에 기존 주소 정보가 없어 새로 생성합니다: postId={}", post.getId());
                Address newAddress = addressService.createAddressForPost(addressDto);
                post.updateAddress(newAddress);
                return newAddress;
            }
            
            // 2. 주소 변경 여부 확인 (최적화 - 불필요한 업데이트 방지)
            if (currentAddress.getAddress().equals(addressDto.getAddress()) && 
                currentAddress.getZonecode().equals(addressDto.getZonecode())) {
                log.info("주소 정보가 동일하여 업데이트 생략: postId={}", post.getId());
                return currentAddress;
            }
            
            // 3. 기존 주소 엔티티 직접 업데이트
            log.info("게시글 주소 업데이트 시작: postId={}, 기존 주소={}, 새 주소={}", 
                    post.getId(), currentAddress.getAddress(), addressDto.getAddress());
            
            return addressService.updateAddress(currentAddress, addressDto);
            
        } catch (Exception e) {
            log.error("게시글 주소 업데이트 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FAILED_TO_UPDATE_POST);
        }
    }

    /**
     * 게시글 태그 업데이트
     */
    private void updatePostTags(Post post, List<String> newTagNames) {
        // 기존 태그 연결 정보 모두 제거
        post.getPostTagList().clear();

        // 새로운 태그 연결 정보 추가
        for (String tagName : newTagNames) {
            Tag tag = tagRepository.findByTagName(tagName)
                    .orElseThrow(() -> {
                        log.error("존재하지 않는 태그: {}", tagName);
                        return new BusinessException(ErrorCode.TAG_NAME_NOT_FOUND);
                    });

            PostTag postTag = PostTag.builder()
                    .post(post)
                    .tag(tag)
                    .build();

            post.getPostTagList().add(postTag);
        }

        log.debug("게시글 태그 업데이트 완료: postId={}, tags={}", post.getId(), newTagNames);
    }

    /**
     * 게시글 이미지 업데이트 - 새 이미지가 있는 경우만 해당 이미지 교체
     * 
     * @param post 게시글 엔티티
     * @param newMainImage 새 메인 이미지 (없으면 기존 유지)
     * @param newDetailImages 새 상세 이미지 목록 (없으면 기존 유지)
     */
    private void updatePostImages(Post post, MultipartFile newMainImage, List<MultipartFile> newDetailImages) {
        // 새 이미지가 없으면 아무 작업 안 함
        boolean hasNewMainImage = newMainImage != null && !newMainImage.isEmpty();
        boolean hasNewDetailImages = newDetailImages != null && !newDetailImages.isEmpty();
        
        if (!hasNewMainImage && !hasNewDetailImages) {
            log.debug("새 이미지가 없어 이미지 업데이트를 건너뜁니다: postId={}", post.getId());
            return;
        }
        
        try {
            // 메인 이미지 업데이트 (새 이미지가 있는 경우만)
            if (hasNewMainImage) {
                // 기존 메인 이미지(썸네일) 찾아서 삭제
                Image oldMainImage = null;
                for (Image image : post.getImageList()) {
                    if (image.isThumbnailStatus()) {
                        oldMainImage = image;
                        break;
                    }
                }
                
                if (oldMainImage != null) {
                    // S3에서 기존 이미지 삭제
                    s3Service.deleteImage(oldMainImage.getImageUrl());
                    // 이미지 목록에서 제거
                    post.getImageList().remove(oldMainImage);
                }
                
                // 새 메인 이미지 업로드
                String newImageUrl = s3Service.uploadImage(newMainImage, "post");
                
                Image thumbnailImage = Image.builder()
                        .post(post)
                        .imageUrl(newImageUrl)
                        .thumbnailStatus(true)
                        .build();
                
                post.getImageList().add(thumbnailImage);
                log.debug("새 메인 이미지 업로드 완료: {}", newImageUrl);
            }
            
            // 상세 이미지 업데이트 (새 이미지가 있는 경우만)
            if (hasNewDetailImages) {
                // 기존 상세 이미지들 찾아서 삭제
                List<Image> oldDetailImages = new ArrayList<>();
                for (Image image : post.getImageList()) {
                    if (!image.isThumbnailStatus()) {
                        oldDetailImages.add(image);
                    }
                }
                
                // S3에서 기존 상세 이미지들 삭제
                for (Image oldImage : oldDetailImages) {
                    s3Service.deleteImage(oldImage.getImageUrl());
                    post.getImageList().remove(oldImage);
                }
                
                // 새 상세 이미지 업로드
                List<String> newImageUrls = s3Service.uploadImages(newDetailImages);
                
                for (String imageUrl : newImageUrls) {
                    Image detailImage = Image.builder()
                            .post(post)
                            .imageUrl(imageUrl)
                            .thumbnailStatus(false)
                            .build();
                    
                    post.getImageList().add(detailImage);
                }
                log.debug("{}개의 새 상세 이미지 업로드 완료", newDetailImages.size());
            }
            
            log.info("게시글 이미지 업데이트 완료: postId={}, 메인 이미지={}, 상세 이미지={}개", 
                   post.getId(), hasNewMainImage, hasNewDetailImages ? newDetailImages.size() : 0);
        } catch (BusinessException e) {
            log.error("이미지 업데이트 중 비즈니스 예외 발생: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("이미지 업데이트 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FAILED_TO_UPDATE_POST);
        }
    }

    /**
     * 게시글 삭제 메소드 (소프트 딜리트)
     * @param postId 삭제할 게시글 ID
     */
    @Transactional
    public void deletePost(Long postId) {

        // postId null 체크 추가
        if (postId == null) {
            log.error("게시글 ID가 null입니다");
            throw new BusinessException(ErrorCode.INVALID_POST_ID);
        }

        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("게시글을 찾을 수 없습니다: postId={}", postId);
                    return new BusinessException(ErrorCode.POST_NOT_FOUND);
                });

        // 이미 삭제된 게시글인지 확인
        if (post.isDeleted()) {
            log.error("이미 삭제된 게시글입니다: postId={}", postId);
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        // 현재 인증된 사용자 정보 가져오기
        // User currentUser = userService.getCurrentUser();

        // 테스트용 하드코딩된 사용자 정보 가져오기 (추후 인증 기능 구현 후 변경 필요)
        User testUser = userRepository.findById(1L)
                .orElseThrow(() -> {
                    log.error("테스트용 사용자를 찾을 수 없습니다");
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        // 게시글 작성자와 현재 사용자가 다를 경우 권한 오류
        if (!post.getUser().getId().equals(testUser.getId())) {
            log.error("게시글 삭제 권한이 없습니다: postId={}, userId={}", postId, testUser.getId());
            throw new BusinessException(ErrorCode.NO_PERMISSION_TO_DELETE);
        }

        try {
            // 소프트 딜리트 수행
            post.delete();
            log.info("게시글 삭제 완료(소프트 딜리트): postId={}", postId);
        } catch (Exception e) {
            log.error("게시글 삭제 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FAILED_TO_DELETE_POST);
        }

        log.info("게시글 삭제 완료(소프트 딜리트): postId={}", postId);
    }

    /**
     * 게시글 공개/비공개 상태 전환 메서드
     * @param postId 상태를 변경할 게시글 ID
     */
    @Transactional
    public void togglePostVisibility(Long postId) {
        // postId null 체크
        if (postId == null) {
            log.error("게시글 ID가 null입니다");
            throw new BusinessException(ErrorCode.INVALID_POST_ID);
        }

        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("게시글을 찾을 수 없습니다: postId={}", postId);
                    return new BusinessException(ErrorCode.POST_NOT_FOUND);
                });

        // 삭제된 게시글인지 확인
        if (post.isDeleted()) {
            log.error("이미 삭제된 게시글입니다: postId={}", postId);
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        // 테스트용 하드코딩된 사용자 정보 가져오기
        User testUser = userRepository.findById(1L)
                .orElseThrow(() -> {
                    log.error("테스트용 사용자를 찾을 수 없습니다");
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        // 게시글 작성자와 현재 사용자가 다를 경우 권한 오류
        if (!post.getUser().getId().equals(testUser.getId())) {
            log.error("게시글 상태 변경 권한이 없습니다: postId={}, userId={}", postId, testUser.getId());
            throw new BusinessException(ErrorCode.NO_PERMISSION_TO_UPDATE);
        }

        try {
            // 공개/비공개 상태 전환
            post.toggleHiddenStatus();
            log.info("게시글 공개 상태 변경 완료: postId={}, 새 상태={}", postId, post.isHiddenStatus() ? "비공개" : "공개");
        } catch (Exception e) {
            log.error("게시글 공개 상태 변경 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FAILED_TO_UPDATE_POST);
        }
    }

    /**
     * 위치 ID 기반 게시글 목록 조회 메소드
     * @param locationInfoId 조회할 위치 정보 ID
     * @return 게시글 목록 응답 DTO
     */
    @Transactional(readOnly = true)
    public List<LocationResponseDto> getPostsIdsByLocationInfoId(Long locationInfoId) {
        if (locationInfoId == null) {
            log.error("위치 정보 ID가 null입니다");
            throw new BusinessException(ErrorCode.INVALID_LOCATION_ID);
        }

        log.info("위치 ID 기반 게시글 ID 조회 시작: locationInfoId={}", locationInfoId);

        // 위치 ID로 게시글 직접 조회 (단일 쿼리 최적화)
        List<Post> posts = postRepository.findActivePostsByLocationInfoId(locationInfoId);

        if (posts.isEmpty()) {
            log.info("해당 위치에 게시글이 없습니다: locationInfoId={}", locationInfoId);
            return Collections.emptyList();
        }

        log.info("위치 ID 기반 게시글 조회 완료: locationInfoId={}, 조회된 게시글 수={}",
                locationInfoId, posts.size());

        // 게시글 ID와 주소 ID만 추출하여 DTO로 변환
        return posts.stream()
                .map(post -> LocationResponseDto.builder()
                        .postId(post.getId())
                        .address(post.getAddress().getAddress())
                        .build())
                .collect(Collectors.toList());
    }
}

>>>>>>> feat/board
