package org.ktb.matajo.controller;

import lombok.RequiredArgsConstructor;
import org.ktb.matajo.dto.user.UserRequestDto;
import org.ktb.matajo.global.common.CommonResponse;
import org.ktb.matajo.service.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ✅ 닉네임 변경 API (PATCH)
    @PatchMapping("/nickname")
    public ResponseEntity<CommonResponse<String>> updateNickname(
            @Valid @RequestBody UserRequestDto request) {

        boolean isUpdated = userService.updateNickname(request.getNickname());

        if (!isUpdated) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(CommonResponse.error("duplicate_nickname", null));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonResponse.success("change_nickname_success", request.getNickname()));
    }

    // ✅ 닉네임 중복 확인 API (GET)
    @GetMapping("/nickname-check")
    public ResponseEntity<CommonResponse<Boolean>> checkNickname(@RequestParam String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(CommonResponse.error("invalid_nickname", null));
        }

        boolean isAvailable = userService.isNicknameAvailable(nickname);
        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonResponse.success("check_nickname_success", isAvailable));
    }
}
