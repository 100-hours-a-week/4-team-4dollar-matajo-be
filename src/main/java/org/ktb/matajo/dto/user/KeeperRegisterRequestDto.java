package org.ktb.matajo.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class KeeperRegisterRequestDto {

    @NotNull(message = "required_terms_of_service")
    private Boolean termsOfService;

    @NotNull(message = "required_privacy_policy")
    private Boolean privacyPolicy;
}
