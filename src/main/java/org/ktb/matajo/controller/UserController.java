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
}
