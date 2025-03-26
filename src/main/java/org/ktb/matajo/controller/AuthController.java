package org.ktb.matajo.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.ktb.matajo.dto.user.KakaoUserInfo;
import org.ktb.matajo.global.common.CommonResponse;
import org.ktb.matajo.security.SecurityUtil;
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
    public ResponseEntity<CommonResponse<?>> kakaoLogin(@RequestBody Map<String, String> request, HttpServletResponse response) {

        String code = request.get("code");
        String accessToken = kakaoAuthService.getAccessToken(code);
        KakaoUserInfo userInfo = kakaoUserService.getUserInfo(accessToken);

        Map<String, String> tokens = userService.processKakaoUser(userInfo);
        String jwtToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken");

        response.addHeader("Set-Cookie", "refreshToken=" + refreshToken + "; HttpOnly; Path=/; Max-Age=1209600"); // 14일

        Map<String, Object> responseData = Map.of(
                "accessToken", jwtToken,
                "refreshToken", refreshToken,
                "nickname", userInfo.getNickname()
        );

        return ResponseEntity.ok(CommonResponse.success("login_success", responseData));
    }

    @PostMapping("/refresh")
    public ResponseEntity<CommonResponse<?>> refreshAccessToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        Map<String, String> tokens = userService.reissueAccessToken(refreshToken);

        response.addHeader("Set-Cookie", "refreshToken=" + tokens.get("refreshToken") + "; HttpOnly; Path=/; Max-Age=1209600");

        return ResponseEntity.ok(CommonResponse.success("token_refresh_success", tokens));
    }


    @GetMapping("/test")
    public ResponseEntity<?> getCurrentUserId() {
        try {
            Long currentUserId = SecurityUtil.getCurrentUserId();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "userId", currentUserId
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}