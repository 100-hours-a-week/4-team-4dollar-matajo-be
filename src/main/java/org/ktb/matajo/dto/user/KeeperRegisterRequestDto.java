package org.ktb.matajo.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class KeeperRegisterRequestDto {

    @NotNull(message = "Agreement to the Terms of Service is required.")
    private Boolean termsOfService;

    @NotNull(message = "Agreement to the Privacy Policy is required.")
    private Boolean privacyPolicy;
}
