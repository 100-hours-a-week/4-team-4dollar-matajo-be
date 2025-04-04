package org.ktb.matajo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ktb.matajo.dto.user.AccessTokenResponseDto;
import org.ktb.matajo.dto.user.KeeperRegisterRequestDto;
import org.ktb.matajo.dto.user.UserRequestDto;
import org.ktb.matajo.global.common.CommonResponse;
import org.ktb.matajo.security.SecurityUtil;
import org.ktb.matajo.service.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 닉네임 수정 요청
    @PatchMapping("/nickname")
    public ResponseEntity<CommonResponse<AccessTokenResponseDto>> updateNickname(
            @Valid @RequestBody UserRequestDto request
    ) {
        Long userId = SecurityUtil.getCurrentUserId(); // 인증된 사용자 ID 추출

        AccessTokenResponseDto newAccessToken = userService.updateNickname(userId, request.getNickname());

        return ResponseEntity.ok(
                CommonResponse.success("change_nickname_success", newAccessToken)
        );
    }

    // 닉네임 사용 가능 여부 확인
    @GetMapping("/nickname")
    public ResponseEntity<CommonResponse<Boolean>> checkNickname(@RequestParam String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(CommonResponse.error("invalid_nickname", null));
        }

        boolean isAvailable = userService.isNicknameAvailable(nickname);

        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonResponse.success("check_nickname_success", isAvailable));
    }

    // 보관인 등록 요청 (JWT로부터 사용자 ID 추출)
    @PostMapping("/keeper")
    public ResponseEntity<CommonResponse<AccessTokenResponseDto>> registerKeeper(
            @Valid @RequestBody KeeperRegisterRequestDto request
    ) {
        Long userId = SecurityUtil.getCurrentUserId(); // 인증된 사용자 ID 추출
        AccessTokenResponseDto response = userService.registerKeeper(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success("keeper_register_success", response));
    }
}
