package org.ktb.matajo.service.image;

import org.ktb.matajo.dto.image.ImageMoveRequestDto;
import org.ktb.matajo.dto.image.ImageMoveResponseDto;
import org.ktb.matajo.dto.image.PresignedUrlRequestDto;
import org.ktb.matajo.dto.image.PresignedUrlResponseDto;

/**
 * 이미지 처리 서비스 인터페이스
 * Lambda 함수를 통한 이미지 업로드 및 관리 기능을 정의합니다.
 */
public interface ImageService {

    /**
     * 이미지 업로드를 위한 Presigned URL 요청
     * 허용 MIME 타입: image/jpeg, image/jpg, image/png, image/webp, image/heic
     *
     * @param requestDto Presigned URL 요청 DTO
     * @return Presigned URL 응답 DTO
     */
    PresignedUrlResponseDto getPresignedUrl(PresignedUrlRequestDto requestDto);

    /**
     * 임시 이미지를 최종 위치로 이동
     * 카테고리는 'post' 또는 'chat'만 가능
     *
     * @param requestDto 이미지 이동 요청 DTO
     * @return 이미지 이동 응답 DTO
     */
    ImageMoveResponseDto moveImage(ImageMoveRequestDto requestDto);
}