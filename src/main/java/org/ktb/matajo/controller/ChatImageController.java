package org.ktb.matajo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.global.common.CommonResponse;
import org.ktb.matajo.service.s3.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/chat/images")
@RequiredArgsConstructor
@Slf4j
public class ChatImageController {
    
    private final S3Service s3Service;
    
    @PostMapping("/upload")
    public ResponseEntity<CommonResponse<String>> uploadChatImage(
            @RequestParam("chatImage") MultipartFile chatImage) {
        
        log.info("채팅 이미지 업로드 요청: {}", chatImage.getOriginalFilename());
        
        // 이미지 유효성 검사
        validateChatImage(chatImage);
        
        // S3에 이미지 업로드
        String imageUrl = s3Service.uploadImage(chatImage, "chat");
        
        log.info("이미지 업로드 완료: {}", imageUrl);
        
        return ResponseEntity.ok(CommonResponse.success("채팅 이미지 업로드 성공", imageUrl));
    }
    
    private void validateChatImage(MultipartFile chatImage) {
        // 파일이 비어있는지 확인
        if (chatImage.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 비어있습니다.");
        }
        
        // 파일 타입 확인
        String contentType = chatImage.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }
        
        // 파일 크기 확인 (10MB 이하)
        if (chatImage.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("이미지 크기는 10MB 이하여야 합니다.");
        }
    }
}