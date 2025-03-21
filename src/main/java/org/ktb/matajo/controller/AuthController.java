package org.ktb.matajo.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.ktb.matajo.dto.user.KakaoUserInfo;
import org.ktb.matajo.service.oauth.KakaoAuthService;
import org.ktb.matajo.service.user.KakaoUserService;
import org.ktb.matajo.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final KakaoAuthService kakaoAuthService;
    private final KakaoUserService kakaoUserService;
    private final UserService userService;

    public AuthController(KakaoAuthService kakaoAuthService, KakaoUserService kakaoUserService, UserService userService) {
        this.kakaoAuthService = kakaoAuthService;
        this.kakaoUserService = kakaoUserService;
        this.userService = userService;
    }

    @PostMapping("/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestBody Map<String, String> request, HttpServletResponse response) {

        String code = request.get("code");
        String accessToken = kakaoAuthService.getAccessToken(code);
        KakaoUserInfo userInfo = kakaoUserService.getUserInfo(accessToken);

        Map<String, String> tokens = userService.processKakaoUser(userInfo);
        String jwtToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken");

        response.addHeader("Set-Cookie", "refreshToken=" + refreshToken + "; HttpOnly; Secure; Path=/; Max-Age=1209600"); // 14일

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "login_success",
                "data", Map.of(
                        "accessToken", jwtToken,
                        "refreshToken", refreshToken,
                        "nickname", userInfo.getNickname()
                )
        ));
    }
}