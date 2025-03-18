package org.ktb.matajo.controller;


import lombok.RequiredArgsConstructor;
import org.ktb.matajo.global.common.CommonResponse;
import org.ktb.matajo.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/nickname-check")
    public ResponseEntity<CommonResponse<Boolean>> checkNickname(@RequestParam String nickname) {
        boolean isAvailable = userService.isNicknameAvailable(nickname);
        return ResponseEntity.ok(CommonResponse.success("닉네임 사용 가능 여부 조회 성공", isAvailable));
    }
}
