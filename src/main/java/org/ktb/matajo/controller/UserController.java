package org.ktb.matajo.controller;

import lombok.RequiredArgsConstructor;
import org.ktb.matajo.dto.user.UserRequestDto;
import org.ktb.matajo.global.common.CommonResponse;
import org.ktb.matajo.service.user.UserService;
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
            return ResponseEntity.status(403)
                    .body(CommonResponse.error("duplicate_nickname", null));
        }

        return ResponseEntity.ok(
                CommonResponse.success("change_nickname_success", request.getNickname()));
    }

    // ✅ 닉네임 중복 확인 API (GET)
    @GetMapping("/nickname-check")
    public ResponseEntity<CommonResponse<Boolean>> checkNickname(@RequestParam String nickname) {
        boolean isAvailable = userService.isNicknameAvailable(nickname);
        return ResponseEntity.ok(CommonResponse.success("닉네임 사용 가능 여부 조회 성공", isAvailable));
    }
}
