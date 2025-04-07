package org.ktb.matajo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.image.ImageMoveRequestDto;
import org.ktb.matajo.dto.image.ImageMoveResponseDto;
import org.ktb.matajo.dto.image.PresignedUrlRequestDto;
import org.ktb.matajo.dto.image.PresignedUrlResponseDto;
import org.ktb.matajo.global.common.CommonResponse;
import org.ktb.matajo.global.error.exception.BusinessException;
import org.ktb.matajo.security.SecurityUtil;
import org.ktb.matajo.service.image.ImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 이미지 업로드 및 관리를 위한 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    /**
     * 이미지 업로드를 위한 Presigned URL을 요청합니다.
     * 
     * @param requestDto Presigned URL 요청 정보
     * @return 생성된 Presigned URL 정보
     */
    @PostMapping("/presigned-url")
    public ResponseEntity<CommonResponse<PresignedUrlResponseDto>> getPresignedUrl(
            @Valid @RequestBody PresignedUrlRequestDto requestDto) {
        
        try {
            // 현재 인증된 사용자 확인 
            Long userId = SecurityUtil.getCurrentUserId();
            log.info("Presigned URL 요청: userId={}, mimeType={}, category={}",
                    userId, requestDto.getMimeType(), requestDto.getCategory());
            
            // 서비스 호출
            PresignedUrlResponseDto responseDto = imageService.getPresignedUrl(requestDto);
            
            log.info("Presigned URL 생성 성공: userId={}, tempKey={}", 
                    userId, responseDto.getTempKey());
            
            return ResponseEntity.ok(CommonResponse.success("created_presignedurl_success", responseDto));
            
        } catch (BusinessException e) {
            log.error("Presigned URL 생성 중 비즈니스 예외: {}", e.getMessage());
            return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(CommonResponse.error(e.getErrorCode().getErrorMessage(), null));
                
        } catch (Exception e) {
            log.error("Presigned URL 생성 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.error("internal_server_error", null));
        }
    }
    
    /**
     * 임시 이미지를 최종 위치(post/chat 카테고리)로 이동합니다.
     * 
     * @param requestDto 이미지 이동 요청 정보
     * @return 이동된 이미지 정보
     */
    @PostMapping("/move")
    public ResponseEntity<CommonResponse<ImageMoveResponseDto>> moveImage(
            @Valid @RequestBody ImageMoveRequestDto requestDto) {
        
        try {
            // 현재 인증된 사용자 확인
            Long userId = SecurityUtil.getCurrentUserId();
            log.info("이미지 이동 요청: userId={}, tempKeys={}, category={}",
                    userId, requestDto.getTempKeys(), requestDto.getCategory());
            
            // 서비스 호출
            ImageMoveResponseDto responseDto = imageService.moveImage(requestDto);
            
            log.info("이미지 이동 성공: userId={}, 이동 성공={}, 실패={}",
                    userId, 
                    responseDto.getMovedImages().size(),
                    responseDto.getFailedImages() != null ? responseDto.getFailedImages().size() : 0);
            
            return ResponseEntity.ok(CommonResponse.success("images_moved_success", responseDto));
            
        } catch (BusinessException e) {
            log.error("이미지 이동 중 비즈니스 예외: {}", e.getMessage());
            return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(CommonResponse.error(e.getErrorCode().getErrorMessage(), null));
                
        } catch (Exception e) {
            log.error("이미지 이동 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.error("internal_server_error", null));
        }
    }
}