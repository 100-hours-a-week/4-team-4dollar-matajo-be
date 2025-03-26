package org.ktb.matajo.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class KeeperRegisterRequestDto {

    @NotNull(message = "서비스 이용 약관 동의는 필수입니다.")
    private Boolean termsOfService;

    @NotNull(message = "개인정보 처리방침 동의는 필수입니다.")
    private Boolean privacyPolicy;
}
