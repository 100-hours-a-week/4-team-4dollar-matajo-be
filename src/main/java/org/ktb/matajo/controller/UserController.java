package org.ktb.matajo.controller;

import lombok.RequiredArgsConstructor;
import org.ktb.matajo.dto.user.NicknameCheckResponse;
import org.ktb.matajo.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/nickname")
    public ResponseEntity<NicknameCheckResponse> checkNickname(@RequestParam String nickname) {
        // 닉네임이 비어있는 경우 (400 Bad Request)
        if (nickname == null || nickname.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new NicknameCheckResponse(false, "invalid_nickname", null));
        }

        // 닉네임 중복 여부 확인
        boolean available = userService.isNicknameAvailable(nickname);

        // JSON 응답 반환
        return ResponseEntity.ok(new NicknameCheckResponse(true, "check_nickname_success", new NicknameCheckResponse.NicknameData(available)));
    }
}
