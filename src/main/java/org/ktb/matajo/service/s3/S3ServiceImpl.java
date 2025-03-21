package org.ktb.matajo.service.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.global.error.code.ErrorCode;
import org.ktb.matajo.global.error.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    /**
     * MultipartFile 형식의 이미지를 S3에 업로드하고 URL 반환
     */

    @Override
    public String uploadImage(MultipartFile file, String category) {
        // 파일 유효성 검증
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_POST_IMAGES);
        }

        try {
            // 원본 파일명 추출 및 고유한 파일명 생성
            String originalFilename = file.getOriginalFilename();
            String extension = getExtensionFromFilename(originalFilename);
            String fileName = category + "/" + UUID.randomUUID() + extension;

            // 메타데이터 설정
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            // S3에 파일 업로드
            amazonS3Client.putObject(
                    new PutObjectRequest(
                            bucketName,
                            fileName,
                            file.getInputStream(),
                            metadata
                    )
            );

            // 업로드된 이미지의 URL 반환
            String imageUrl = amazonS3Client.getUrl(bucketName, fileName).toString();
            log.info("이미지 업로드 완료: {}, 원본 파일명: {}", imageUrl, originalFilename);
            
            return imageUrl;
        } catch (IOException e) {
            log.error("이미지 업로드 중 IO 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FAILED_TO_UPLOAD_IMAGE);
        } catch (Exception e) {
            log.error("이미지 업로드 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FAILED_TO_UPLOAD_IMAGE);
        }
    }

    /**
     * 다수의 MultipartFile 이미지를 S3에 업로드하고 URL 목록 반환
     */
    @Override
    public List<String> uploadImages(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String imageUrl = uploadImage(file, "post");
                imageUrls.add(imageUrl);
            }
        }

        return imageUrls;
    }

    /**
     * S3에서 이미지 삭제
     */
    @Override
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        try {
            // URL에서 키(파일 경로) 추출
            String key = extractKeyFromUrl(imageUrl);
            if (key == null) {
                log.warn("S3 키를 추출할 수 없는 URL: {}", imageUrl);
                return;
            }

            // S3에서 이미지 삭제
            amazonS3Client.deleteObject(bucketName, key);
            log.info("이미지 삭제 완료: {}", imageUrl);
        } catch (Exception e) {
            log.error("이미지 삭제 중 오류 발생: {}", e.getMessage(), e);
            // 삭제 실패는 예외를 발생시키지 않고 로그만 남김
        }
    }

    /**
     * 파일명에서 확장자 추출
     */
    private String getExtensionFromFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return ".jpg"; // 기본 확장자
        }
        
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex);
        }
        
        return ".jpg"; // 확장자가 없는 경우 기본 확장자 반환
    }

    /**
     * S3 URL에서 키(파일 경로) 추출
     */
    private String extractKeyFromUrl(String imageUrl) {
        String bucketUrl = amazonS3Client.getUrl(bucketName, "").toString();
        if (imageUrl.startsWith(bucketUrl)) {
            return imageUrl.substring(bucketUrl.length());
        }
        return null;
    }
}