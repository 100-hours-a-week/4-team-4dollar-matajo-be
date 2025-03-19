package org.ktb.matajo.dto.user;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Setter
public class UserRequestDto {

    @NotBlank(message = "invalid_nickname") // ✅ API 명세에 맞게 수정
    @Size(min = 2, max = 20, message = "invalid_nickname") // ✅ API 명세에 맞게 수정
    private String nickname;
}
