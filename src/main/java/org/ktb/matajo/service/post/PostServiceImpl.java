package org.ktb.matajo.service.post;

import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.location.LocationDealResponseDto;
import org.ktb.matajo.dto.location.LocationPostResponseDto;
import org.ktb.matajo.dto.post.*;
import org.ktb.matajo.entity.*;
import org.ktb.matajo.global.error.code.ErrorCode;
import org.ktb.matajo.global.error.exception.BusinessException;
import org.ktb.matajo.repository.LocationInfoRepository;
import org.ktb.matajo.repository.PostRepository;
import org.ktb.matajo.repository.TagRepository;
import org.ktb.matajo.repository.UserRepository;
import org.ktb.matajo.security.SecurityUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final LocationInfoRepository locationInfoRepository;

    private final AddressService addressService;

    /**
     * 게시글 목록 조회 메소드
     */
    @Override
    public List<PostListResponseDto> getPostList(int offset, int limit) {
        // 요청 파라미터 검증
        if (offset < 0 || limit <= 0) {
            throw new BusinessException(ErrorCode.INVALID_OFFSET_OR_LIMIT);
        }

        // 페이징 처리된 게시글 목록 조회
        Pageable pageable = PageRequest.of(offset, limit);
        List<Post> posts = postRepository.findAllActivePostsOrderByCreatedAtDesc(pageable);

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

        // 주소 가공: 시군구 + 법정동 이름
        Address address = post.getAddress();
        String formattedAddress = String.format("%s %s",
                address.getSigungu() != null ? address.getSigungu() : "",
                address.getBname2() != null ? address.getBname2() : "").trim();

        return PostListResponseDto.builder()
                .postId(post.getId())
                .postTitle(post.getTitle())
                .postMainImage(mainImageUrl)
                .postAddress(formattedAddress)
                .preferPrice(post.getPreferPrice())
                .postTags(tags)
                .build();
    }


    /**
     * 게시글 등록 메소드 - MultipartFile로 이미지 처리
     * @param requestDto 게시글 정보
     * @return 생성된 게시글 ID를 담은 응답 DTO
     */
    @Override
    @Transactional
    public PostCreateResponseDto createPost(PostCreateRequestDto requestDto, Long userId) {
        // 요청 데이터 유효성 검증
        validatePostRequest(requestDto);

        // 유저 정보 가져오기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없습니다");
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        if (user.getRole() == UserType.USER) {
            throw new BusinessException(ErrorCode.REQUIRED_PERMISSION);
        }

        log.info("사용자 정보: ID={}, 닉네임={}", user.getId(), user.getNickname());

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
                .user(user)
                .title(requestDto.getPostTitle())
                .content(requestDto.getPostContent())
                .preferPrice(requestDto.getPreferPrice())
                .hiddenStatus(false) // 처음 등록 시 항상 false로 설정
                .discountRate(0) // 처음 등록 시 항상 0으로 설정
                .address(address)
                .build();

        Post savedPost = postRepository.save(post);

        log.info("게시글 생성 완료: ID={}, 제목={}", savedPost.getId(), savedPost.getTitle());
        // 태그 처리
        if (requestDto.getPostTags() != null && !requestDto.getPostTags().isEmpty()) {
            processPostTags(savedPost, requestDto.getPostTags());
        }

        // 이미지 처리
        processMultipartImages(savedPost, requestDto.getMainImage(), requestDto.getDetailImages());

        return PostCreateResponseDto.builder()
                .postId(savedPost.getId())
                .build();
    }

    /**
     * 게시글 요청 데이터 유효성 검증 (MultipartFile 버전)
     */
    private void validatePostRequest(PostCreateRequestDto postData) {
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
        if (postData.getMainImage() == null || postData.getMainImage().isBlank()) {
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
    private void processMultipartImages(Post post, String mainImageUrl, List<String> detailImageUrls) {
        try {
            // 메인 이미지 처리 (썸네일로 설정)
            if (mainImageUrl != null && !mainImageUrl.isBlank()) {
                Image thumbnailImage = Image.builder()
                        .post(post)
                        .imageUrl(mainImageUrl)
                        .thumbnailStatus(true)  // 메인 이미지는 썸네일로 설정
                        .build();

                post.getImageList().add(thumbnailImage);
                log.info("메인 이미지(썸네일) 처리 완료: {}", mainImageUrl);
            }

            // 상세 이미지 처리
            if (detailImageUrls != null && !detailImageUrls.isEmpty()) {
                for (String imageUrl : detailImageUrls) {
                    Image image = Image.builder()
                            .post(post)
                            .imageUrl(imageUrl)
                            .thumbnailStatus(false)  // 상세 이미지는 썸네일 아님
                            .build();

                    post.getImageList().add(image);
                }
                log.info("{}개의 상세 이미지 처리 완료", detailImageUrls.size());
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
    @Override
    public PostDetailResponseDto getPostDetail(Long postId) {
        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("게시글을 찾을 수 없습니다: postId={}", postId);
                    return new BusinessException(ErrorCode.POST_NOT_FOUND);
                });
        // 게시글 작성자 정보 가져오기        
        // 현재 사용자 정보
        Long userId = SecurityUtil.getCurrentUserId();

        // 삭제된 게시글인지 확인
        if (post.isDeleted()) {
            log.error("이미 삭제된 게시글입니다: postId={}", postId);
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        // 숨김 처리된 게시글인지 확인
        if (post.isHiddenStatus() && !post.getUser().getId().equals(userId)) {
            log.error("숨김 처리된 게시글이며 작성자가 아닙니다: postId={}, userId={}", postId, userId);
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
                    .editable(post.getUser().getId().equals(userId))
                    .build();
        } catch (Exception e) {
            log.error("게시글 상세 정보 DTO 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FAILED_TO_GET_POST_DETAIL);
        }
    }



    /**
     * 게시글 수정 메서드 - 선택적 필드 업데이트
     * @param postId 수정할 게시글 ID
     * @param requestDto 선택적 수정 정보 DTO
     * @return 수정된 게시글 ID 응답 DTO
     */
    @Override
    @Transactional
    public PostEditResponseDto updatePost(Long postId, PostEditRequestDto requestDto, Long userId) {
        // 게시글 ID 검증
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

        // 사용자 정보 가져오기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없습니다");
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        if (user.getRole() == UserType.USER) {
            throw new BusinessException(ErrorCode.REQUIRED_PERMISSION);
        }

        // 게시글 작성자 - 현재 사용자 비교
        if (!post.getUser().getId().equals(user.getId())) {
            log.error("게시글 수정 권한이 없습니다: postId={}, userId={}", postId, user.getId());
            throw new BusinessException(ErrorCode.NO_PERMISSION_TO_UPDATE);
        }

        try {
            // 선택적으로 주소 업데이트
            if (requestDto.getPostAddressData() != null) {
                updatePostAddress(post, requestDto.getPostAddressData());
            }

            // 선택적으로 태그 업데이트
            if (requestDto.getPostTags() != null && !requestDto.getPostTags().isEmpty()) {
                updatePostTags(post, requestDto.getPostTags());
            }

            // 선택적으로 이미지 업데이트
            if ((requestDto.getMainImage() != null && !requestDto.getMainImage().isBlank()) || 
                (requestDto.getDetailImages() != null && !requestDto.getDetailImages().isEmpty())) {
                updatePostImages(post, requestDto.getMainImage(), requestDto.getDetailImages());
            }

            // 각 필드 선택적 업데이트
            if (requestDto.getPostTitle() != null && !requestDto.getPostTitle().isBlank()) {
                post.updateTitle(requestDto.getPostTitle());
            }
            
            if (requestDto.getPostContent() != null && !requestDto.getPostContent().isBlank()) {
                post.updateContent(requestDto.getPostContent());
            }

            // 가격 업데이트 (null이 아니고 0보다 큰 경우에만)
            if (requestDto.getPreferPrice() != null && requestDto.getPreferPrice() > 0) {
                int currentPrice = post.getPreferPrice();
                int newPrice = requestDto.getPreferPrice();
                post.updatePreferPrice(newPrice);
    
                // 할인율 자동 계산 - 수정된 가격이 기존 가격보다 작을 경우
                if (newPrice < currentPrice) {
                    float calculatedDiscountRate = (float) ((currentPrice - newPrice) / (float) currentPrice) * 100;
                    post.updateDiscountRate(calculatedDiscountRate);
                }
            }

            // 숨김 상태 업데이트 (null이 아닌 경우에만)
            if (requestDto.getHiddenStatus() != null) {
                post.updateHiddenStatus(requestDto.getHiddenStatus());
            }

            log.info("게시글 선택적 수정 완료: postId={}", postId);
            
            return PostEditResponseDto.builder()
                    .postId(post.getId())
                    .build();
                    
        } catch (BusinessException e) {
            log.error("게시글 수정 중 비즈니스 예외 발생: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("게시글 수정 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FAILED_TO_UPDATE_POST);
        }
    }

    /**
     * 게시글 주소 선택적 업데이트
     */
    private void updatePostAddress(Post post, AddressDto addressDto) {
        try {
            // 1. 기존 주소 존재 확인
            Address currentAddress = post.getAddress();
            if (currentAddress == null) {
                // 기존 주소가 없는 경우 (드문 경우) 새로 생성
                log.info("게시글에 기존 주소 정보가 없어 새로 생성합니다: postId={}", post.getId());
                Address newAddress = addressService.createAddressForPost(addressDto);
                post.updateAddress(newAddress);
                return;
            }
            
            // 2. 주소 변경 여부 확인 (최적화 - 불필요한 업데이트 방지)
            if (currentAddress.getAddress().equals(addressDto.getAddress()) && 
                currentAddress.getZonecode().equals(addressDto.getZonecode())) {
                log.info("주소 정보가 동일하여 업데이트 생략: postId={}", post.getId());
                return;
            }
            
            // 3. 기존 주소 엔티티 직접 업데이트
            log.info("게시글 주소 업데이트 시작: postId={}, 기존 주소={}, 새 주소={}", 
                    post.getId(), currentAddress.getAddress(), addressDto.getAddress());
            
            addressService.updateAddress(currentAddress, addressDto);
            
        } catch (Exception e) {
            log.error("게시글 주소 업데이트 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FAILED_TO_UPDATE_POST);
        }
    }

    /**
     * 게시글 태그 선택적 업데이트
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
     * 게시글 이미지 선택적 업데이트
     *
     * @param post 게시글 엔티티
     * @param newMainImageUrl 새 메인 이미지 (없으면 기존 유지)
     * @param newDetailImageUrls 새 상세 이미지 목록 (없으면 기존 유지)
     */
    private void updatePostImages(Post post, String newMainImageUrl, List<String> newDetailImageUrls) {
        // 새 이미지가 없으면 아무 작업 안 함
        boolean hasNewMainImage = newMainImageUrl != null && !newMainImageUrl.isBlank();
        boolean hasNewDetailImages = newDetailImageUrls != null && !newDetailImageUrls.isEmpty();

        if (!hasNewMainImage && !hasNewDetailImages) {
            log.debug("새 이미지가 없어 이미지 업데이트를 건너뜁니다: postId={}", post.getId());
            return;
        }

        try {
            // 이미지 엔티티 목록 클리어를 위한 복사본 생성
            List<Image> imagesToRemove = new ArrayList<>();

            // 메인 이미지 업데이트 (새 이미지가 있는 경우만)
            if (hasNewMainImage) {
                // 기존 메인 이미지(썸네일) 찾기
                for (Image image : post.getImageList()) {
                    if (image.isThumbnailStatus()) {
                        imagesToRemove.add(image);
                    }
                }

                // 기존 메인 이미지 제거
                post.getImageList().removeAll(imagesToRemove);

                // 새 메인 이미지 추가
                Image thumbnailImage = Image.builder()
                        .post(post)
                        .imageUrl(newMainImageUrl)
                        .thumbnailStatus(true)
                        .build();

                post.getImageList().add(thumbnailImage);
                log.debug("새 메인 이미지 업데이트 완료: {}", newMainImageUrl);
            }

            // 상세 이미지 업데이트 (새 이미지가 있는 경우만)
            if (hasNewDetailImages) {
                // 기존 상세 이미지들 찾기
                imagesToRemove.clear();
                for (Image image : post.getImageList()) {
                    if (!image.isThumbnailStatus()) {
                        imagesToRemove.add(image);
                    }
                }

                // 기존 상세 이미지 제거
                post.getImageList().removeAll(imagesToRemove);

                // 새 상세 이미지 추가
                for (String imageUrl : newDetailImageUrls) {
                    if (imageUrl != null && !imageUrl.isBlank()) {
                        Image detailImage = Image.builder()
                                .post(post)
                                .imageUrl(imageUrl)
                                .thumbnailStatus(false)
                                .build();

                        post.getImageList().add(detailImage);
                    }
                }
                log.debug("{}개의 새 상세 이미지 업데이트 완료", newDetailImageUrls.size());
            }

            log.info("게시글 이미지 업데이트 완료: postId={}, 메인 이미지={}, 상세 이미지={}개",
                    post.getId(), hasNewMainImage, hasNewDetailImages ? newDetailImageUrls.size() : 0);
        } catch (Exception e) {
            log.error("이미지 업데이트 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FAILED_TO_UPDATE_POST);
        }
    }

    /**
     * 게시글 삭제 메소드 (소프트 딜리트)
     * @param postId 삭제할 게시글 ID
     */
    @Override
    @Transactional
    public void deletePost(Long postId, Long userId) {

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

        // 사용자 정보 가져오기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없습니다");
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        if (user.getRole() == UserType.USER) {
            throw new BusinessException(ErrorCode.REQUIRED_PERMISSION);
        }

        // 게시글 작성자와 현재 사용자가 다를 경우 권한 오류
        if (!post.getUser().getId().equals(user.getId())) {
            log.error("게시글 삭제 권한이 없습니다: postId={}, userId={}", postId, user.getId());
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
    @Override
    @Transactional
    public void togglePostVisibility(Long postId, Long userId) {
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없습니다");
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        if (user.getRole() == UserType.USER) {
            throw new BusinessException(ErrorCode.REQUIRED_PERMISSION);
        }

        // 게시글 작성자와 현재 사용자가 다를 경우 권한 오류
        if (!post.getUser().getId().equals(user.getId())) {
            log.error("게시글 상태 변경 권한이 없습니다: postId={}, userId={}", postId, user.getId());
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
    @Override
    public List<LocationPostResponseDto> getPostsIdsByLocationInfoId(Long locationInfoId) {
        if (locationInfoId == null) {
            log.error("위치 정보 ID가 null입니다");
            throw new BusinessException(ErrorCode.INVALID_LOCATION_ID);
        }

        log.info("위치 ID 기반 게시글 ID 조회 시작: locationInfoId={}", locationInfoId);
        //locationInfoId로 동일한 cityDistrict를 가진 id 가져오기
        List<Long> locationInfoIds = locationInfoRepository.findIdsInSameDistrict(locationInfoId);

        if(locationInfoIds.isEmpty()){
            log.info("해당 dirstrict에 위치 정보가 없습니다: locationInfoId={}",locationInfoId);
            return Collections.emptyList();
        }

        // 위치 ID로 게시글 직접 조회 (단일 쿼리 최적화)
        List<Post> posts = postRepository.findActivePostsByLocationInfoIds(locationInfoIds);

        if (posts.isEmpty()) {
            log.info("해당 위치에 게시글이 없습니다: locationInfoId={}", locationInfoId);
            return Collections.emptyList();
        }

        log.info("위치 ID 기반 게시글 조회 완료: locationInfoId={}, 조회된 게시글 수={}",
            locationInfoId, posts.size());

        // 3. 게시글 정보를 DTO로 변환
            return posts.stream()
                .map(post -> LocationPostResponseDto.builder()
                    .postId(post.getId())
                    .title(post.getTitle())
                    .address(post.getAddress().getAddress())
                    .build())
                .collect(Collectors.toList());
    }


    @Override
    public List<LocationDealResponseDto> getTopDiscountedPosts(Long locationInfoId) {
        log.info("지역 특가 게시글 조회 시작: locationInfoId={}", locationInfoId);

        if (locationInfoId == null) {
            log.error("위치 정보 ID가 null입니다");
            throw new BusinessException(ErrorCode.INVALID_LOCATION_ID);
        }

        List<Post> topDiscountedPosts = postRepository.findTopDiscountedPostsByLocationInfoId(locationInfoId);

        if (topDiscountedPosts.isEmpty() || 
            topDiscountedPosts.stream().allMatch(post -> post.getDiscountRate() == 0)) {
            log.info("해당 지역의 할인 게시글이 없거나 모두 할인율이 0입니다: locationInfoId={}", locationInfoId);
            return Collections.emptyList();
        }

        // DTO 변환
        List<LocationDealResponseDto> dealResponses = topDiscountedPosts.stream()
            .map(post -> LocationDealResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .discount(String.format("-%d%%", Math.round(post.getDiscountRate())))
                .imageUrl(post.getImageList().stream()
                    .filter(Image::isThumbnailStatus)
                    .findFirst()
                    .map(Image::getImageUrl)
                    .orElse(null))
                .build())
            .collect(Collectors.toList());

        log.info("지역 특가 게시글 조회 완료: locationInfoId={}, 조회된 게시글 수={}", 
            locationInfoId, dealResponses.size());

        return dealResponses;
    }
    // 내 보관소 조회
    @Override
    @Transactional(readOnly = true)
    public List<PostResponseDto> getMyPosts(Long userId, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        List<Post> posts = postRepository.findByUserId(userId, pageable);

        return posts.stream()
                .map(post -> PostResponseDto.builder()
                        .postId(post.getId())
                        .postTitle(post.getTitle())
                        .postMainImage(post.getImageList().stream()
                                .filter(Image::isThumbnailStatus)
                                .findFirst()
                                .map(Image::getImageUrl)
                                .orElse(null))
                        .postAddress(post.getAddress().getSigungu() + " " + post.getAddress().getBname())
                        .preferPrice(post.getPreferPrice())
                        .hiddenStatus(post.isHiddenStatus())
                        .createdAt(post.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 카테고리별 태그 필터링 기반 게시글 목록 조회
     * 각 카테고리 내 태그는 OR 조건, 카테고리 간 태그는 AND 조건으로 필터링
     */
    @Override
    public List<PostListResponseDto> getPostsByTagsWithCategoryLogic(List<String> tagNames, int offset, int limit) {
        // 요청 파라미터 검증
        if (offset < 0 || limit <= 0) {
            throw new BusinessException(ErrorCode.INVALID_OFFSET_OR_LIMIT);
        }
        
        if (tagNames == null || tagNames.isEmpty()) {
            log.info("태그 필터가 없어 일반 게시글 목록을 반환합니다.");
            return getPostList(offset, limit);
        }
        
        log.info("카테고리 기반 태그 필터링 시작: tags={}, offset={}, limit={}", tagNames, offset, limit);
        
        try {
            // 1. 태그 이름 목록으로 태그 정보 조회
            List<Tag> tags = tagRepository.findByTagNameIn(tagNames);
            if (tags.isEmpty()) {
                log.info("유효한 태그가 없습니다: tagNames={}", tagNames);
                return Collections.emptyList();
            }
            
            // 2. 태그를 카테고리별로 그룹화
            Map<Long, List<Tag>> tagsByCategory = tags.stream()
                    .collect(Collectors.groupingBy(Tag::getTagCategoryId));
            
            log.info("카테고리별 태그 그룹화: {}", tagsByCategory.keySet());
            
            // 3. 초기 결과 집합으로 사용할 게시글 ID 집합 (아직 미설정)
            Set<Long> resultPostIds = null;
            
            // 4. 각 카테고리별로 OR 조건으로 게시글 찾고, 카테고리 간에는 AND 조건 적용
            for (Map.Entry<Long, List<Tag>> entry : tagsByCategory.entrySet()) {
                Long categoryId = entry.getKey();
                List<Tag> categoryTags = entry.getValue();
                
                // 카테고리 내 태그 ID 목록
                List<Long> categoryTagIds = categoryTags.stream()
                        .map(Tag::getId)
                        .collect(Collectors.toList());
                
                log.debug("카테고리 {} 태그 ID: {}", categoryId, categoryTagIds);
                
                // 카테고리 내 태그가 포함된 게시글 ID 조회 (OR 조건)
                List<Long> categoryPostIds = postRepository.findPostIdsByTagIds(categoryTagIds);
                
                if (categoryPostIds.isEmpty()) {
                    // 해당 카테고리의 태그를 가진 게시글이 없으면 최종 결과도 없음 (AND 조건)
                    log.info("카테고리 {}의 태그를 가진 게시글이 없습니다", categoryId);
                    return Collections.emptyList();
                }
                
                // 결과 집합 초기화 또는 교집합 처리 (AND 조건)
                if (resultPostIds == null) {
                    resultPostIds = new HashSet<>(categoryPostIds);
                } else {
                    resultPostIds.retainAll(categoryPostIds); // 교집합 구하기
                    
                    // 교집합 이후 결과가 없으면 즉시 반환
                    if (resultPostIds.isEmpty()) {
                        log.info("모든 카테고리 조건을 만족하는 게시글이 없습니다");
                        return Collections.emptyList();
                    }
                }
            }
            
            // 결과가 없으면 빈 목록 반환
            if (resultPostIds == null || resultPostIds.isEmpty()) {
                log.info("필터링 조건을 만족하는 게시글이 없습니다");
                return Collections.emptyList();
            }
            
            log.info("카테고리 필터링 후 게시글 수: {}", resultPostIds.size());
            
            // 5. 최종 결과 게시글 ID로 게시글 조회 (페이징 적용)
            Pageable pageable = PageRequest.of(offset, limit);
            List<Post> posts = postRepository.findByPostIds(new ArrayList<>(resultPostIds), pageable);
            
            // 6. 엔티티를 DTO로 변환
            List<PostListResponseDto> result = posts.stream()
                    .map(this::convertToPostResponseDto)
                    .collect(Collectors.toList());
            
            log.info("카테고리 기반 태그 필터링 완료: 결과 게시글 수={}", result.size());
            
            return result;
        } catch (Exception e) {
            log.error("카테고리별 태그 필터링 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

}

