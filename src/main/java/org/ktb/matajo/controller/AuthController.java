package org.ktb.matajo.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.ktb.matajo.dto.user.LoginRequestDto;
import org.ktb.matajo.dto.user.LoginResponseDto;
import org.ktb.matajo.dto.user.TokenResponseDto;
import org.ktb.matajo.global.common.CommonResponse;
import org.ktb.matajo.security.SecurityUtil;
import org.ktb.matajo.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/kakao")
    public ResponseEntity<CommonResponse<LoginResponseDto>> kakaoLogin(@RequestBody LoginRequestDto request, HttpServletResponse response) {
        String code = request.getCode();
        LoginResponseDto loginData = userService.loginWithKakao(code, response);

        return ResponseEntity.ok(CommonResponse.success("login_success", loginData));
    }

    @PostMapping("/refresh")
    public ResponseEntity<CommonResponse<TokenResponseDto>> refreshAccessToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        TokenResponseDto tokens = userService.reissueAccessToken(refreshToken);

        response.addHeader("Set-Cookie", "refreshToken=" + tokens.getRefreshToken()
                + "; HttpOnly; Path=/; Max-Age=1209600");

        return ResponseEntity.ok(CommonResponse.success("access_token_reissue_success", tokens));
    }

    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<?>> logout(HttpServletResponse response) {
        userService.logout();

        response.addHeader("Set-Cookie", "refreshToken=; HttpOnly; Path=/; Max-Age=0");

        return ResponseEntity.ok(CommonResponse.success("logout_success", null));
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