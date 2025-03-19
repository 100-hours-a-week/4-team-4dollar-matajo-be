package org.ktb.matajo.service.post;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.post.PostCreateRequestDto;
import org.ktb.matajo.dto.post.PostCreateResponseDto;
import org.ktb.matajo.dto.post.PostListResponseDto;
import org.ktb.matajo.entity.*;
import org.ktb.matajo.global.error.code.ErrorCode;
import org.ktb.matajo.global.error.exception.BusinessException;
import org.ktb.matajo.repository.PostRepository;
import org.ktb.matajo.repository.TagRepository;
import org.ktb.matajo.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

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

        if (posts.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_POSTS_PAGE);
        }

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

        // 테스트용 하드코딩된 사용자 정보 가져오기 (kakaoId가 987654321인 사용자)
        User testUser = userRepository.findByKakaoId(987654321L)
                .orElseThrow(() -> {
                    log.error("테스트용 사용자를 찾을 수 없습니다");
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        log.info("테스트 사용자 정보: ID={}, 닉네임={}", testUser.getId(), testUser.getNickname());

        //TODO:keeper인 사람만 게시글을 등록할 수 있다.

        // 주소 정보 처리
        Address address;
        try {
            address = addressService.saveOrGetAddress(requestDto.getPostAddressData());
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
                .hiddenStatus(requestDto.isHiddenStatus())
                .discountRate(requestDto.getDiscountRate())
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

        // 희망 가격 검증
        // TODO : 0원도 가능한가?
        if (postData.getPreferPrice() < 0) {
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
                        return new BusinessException(ErrorCode.INVALID_TAG_NAME);
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
                String mainImageUrl = s3Service.uploadImage(mainImage);

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
}
