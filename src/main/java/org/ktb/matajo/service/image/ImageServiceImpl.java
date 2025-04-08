package org.ktb.matajo.service.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.image.ImageMoveRequestDto;
import org.ktb.matajo.dto.image.ImageMoveResponseDto;
import org.ktb.matajo.dto.image.MovedImageDto;
import org.ktb.matajo.dto.image.PresignedUrlRequestDto;
import org.ktb.matajo.dto.image.PresignedUrlResponseDto;
import org.ktb.matajo.global.error.code.ErrorCode;
import org.ktb.matajo.global.error.exception.BusinessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 이미지 처리 서비스 구현 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final RestTemplate restTemplate;

    private static final String LAMBDA_UPLOAD_URL = "https://cggn6k5n62.execute-api.ap-northeast-2.amazonaws.com/dev/temp-upload";
    private static final String LAMBDA_MOVE_URL = "https://cggn6k5n62.execute-api.ap-northeast-2.amazonaws.com/dev/move-image";

    // 허용되는 이미지 MIME 타입 목록
    private static final Set<String> ALLOWED_MIME_TYPES = new HashSet<>(
            Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp", "image/heic"));

    @Override
    public PresignedUrlResponseDto getPresignedUrl(PresignedUrlRequestDto requestDto) {
        log.info("Presigned URL 요청: mimeType={}, category={}",
                requestDto.getMimeType(), requestDto.getCategory());

        // MIME 타입 추가 검증
        validateMimeType(requestDto.getMimeType());

        try {
            // DTO를 Map으로 변환 (Lambda 요청 형식에 맞춤)
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("mime_type", requestDto.getMimeType());
            requestMap.put("filename", requestDto.getFilename());
            requestMap.put("category", requestDto.getCategory());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestMap, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    LAMBDA_UPLOAD_URL,
                    entity,
                    Map.class
            );

            log.debug("Lambda 응답 상태: {}", response.getStatusCode());

            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !Boolean.TRUE.equals(responseBody.get("success"))) {
                String errorMessage = responseBody != null ? (String) responseBody.get("message") : "응답이 비어있습니다";
                log.error("Lambda 함수 오류 응답: {}", errorMessage);
                throw new BusinessException(ErrorCode.FAILED_TO_UPLOAD_IMAGE);
            }

            // 응답 데이터 추출 및 DTO로 변환
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

            return PresignedUrlResponseDto.builder()
                    .presignedUrl((String) data.get("presigned_url"))
                    .imageUrl((String) data.get("image_url"))
                    .tempKey((String) data.get("temp_key"))
                    .build();

        } catch (HttpClientErrorException e) {
            log.error("Lambda 함수 클라이언트 오류: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        } catch (HttpServerErrorException e) {
            log.error("Lambda 함수 서버 오류: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FAILED_TO_UPLOAD_IMAGE);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Presigned URL 요청 중 예상치 못한 오류: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FAILED_TO_UPLOAD_IMAGE);
        }
    }

    @Override
    public ImageMoveResponseDto moveImage(ImageMoveRequestDto requestDto) {
        log.info("이미지 이동 요청: tempKeys={}, category={}",
                requestDto.getTempKeys(), requestDto.getCategory());

        try {
            // DTO를 Map으로 변환 (Lambda 요청 형식에 맞춤)
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("temp_keys", requestDto.getTempKeys());
            requestMap.put("category", requestDto.getCategory());
            if (requestDto.getMainFlags() != null) {
                requestMap.put("main_flags", requestDto.getMainFlags());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestMap, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    LAMBDA_MOVE_URL,
                    entity,
                    Map.class
            );

            log.debug("Lambda 응답 상태: {}", response.getStatusCode());

            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !Boolean.TRUE.equals(responseBody.get("success"))) {
                String errorMessage = responseBody != null ? (String) responseBody.get("message") : "응답이 비어있습니다";
                log.error("Lambda 함수 오류 응답: {}", errorMessage);
                throw new BusinessException(ErrorCode.FAILED_TO_UPLOAD_IMAGE);
            }

            // 응답 데이터 추출 및 DTO로 변환
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

            // 이동된 이미지 목록 변환
            List<MovedImageDto> movedImages = new ArrayList<>();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> movedList = (List<Map<String, Object>>) data.get("moved_images");
            if (movedList != null) {
                for (Map<String, Object> moved : movedList) {
                    movedImages.add(MovedImageDto.builder()
                            .tempKey((String) moved.get("temp_key"))
                            .imageUrl((String) moved.get("image_url"))
                            .isMainImage(Boolean.TRUE.equals(moved.get("is_main_image")))
                            .build());
                }
            }

            // 실패한 이미지 목록 변환
            @SuppressWarnings("unchecked")
            List<String> failedImages = (List<String>) data.get("failed_images");

            return ImageMoveResponseDto.builder()
                    .movedImages(movedImages)
                    .failedImages(failedImages != null ? failedImages : new ArrayList<>())
                    .build();

        } catch (HttpClientErrorException e) {
            log.error("Lambda 함수 클라이언트 오류: {}", e.getMessage(), e);
            if (e.getStatusCode().value() == 404) {
                throw new BusinessException(ErrorCode.FAILED_TO_UPLOAD_IMAGE);
            }
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        } catch (HttpServerErrorException e) {
            log.error("Lambda 함수 서버 오류: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FAILED_TO_UPLOAD_IMAGE);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("이미지 이동 중 예상치 못한 오류: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FAILED_TO_UPLOAD_IMAGE);
        }
    }

    /**
     * MIME 타입 유효성 검사
     * @param mimeType 검사할 MIME 타입
     * @throws BusinessException 유효하지 않은 MIME 타입일 경우 예외 발생
     */
    private void validateMimeType(String mimeType) {
        if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType)) {
            log.error("지원하지 않는 이미지 형식: {}", mimeType);
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
    }
}