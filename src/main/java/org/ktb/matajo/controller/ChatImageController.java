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
        
        // S3에 이미지 업로드
        String imageUrl = s3Service.uploadImage(chatImage, "chat");
        
        log.info("이미지 업로드 완료: {}", imageUrl);
        
        return ResponseEntity.ok(CommonResponse.success("채팅 이미지 업로드 성공", imageUrl));
    }
}