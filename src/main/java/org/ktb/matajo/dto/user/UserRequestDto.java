package org.ktb.matajo.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UserRequestDto {

    @NotBlank(message = "invalid_nickname") // ✅ API 명세에 맞게 수정
    @Size(min = 1, max = 10, message = "invalid_nickname") // ✅ API 명세에 맞게 수정
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "invalid_nickname")
    private String nickname;
}
