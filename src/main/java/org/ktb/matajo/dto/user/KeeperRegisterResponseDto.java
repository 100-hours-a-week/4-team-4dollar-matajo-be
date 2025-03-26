package org.ktb.matajo.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KeeperRegisterResponseDto {
    // 보관인 등록 후 클라이언트에게 반환할 액세스 토큰
    private String accessToken;
}
