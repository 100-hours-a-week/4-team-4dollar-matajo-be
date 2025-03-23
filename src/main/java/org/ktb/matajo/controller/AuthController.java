package org.ktb.matajo.controller;

import java.util.Map;

import org.ktb.matajo.dto.user.KakaoUserInfo;
import org.ktb.matajo.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final UserService userService;

  public AuthController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/kakao")
  public ResponseEntity<?> kakaoLogin(
          @RequestBody Map<String, String> request, HttpServletResponse response) {

    // 실제 카카오 로그인 대신 더미 유저로 대체
    KakaoUserInfo userInfo = new KakaoUserInfo(
            9999L, "dummy@example.com", "더미유저", "010-1234-5678", "USER"
    );

    // 더미 유저 처리
    userService.processKakaoUser(userInfo);

    // 로그인 성공 응답
    return ResponseEntity.ok(
            Map.of(
                    "success", true,
                    "message", "login_success",
                    "data", Map.of(
                            "nickname", userInfo.getNickname()
                    )
            )
    );
  }
}
