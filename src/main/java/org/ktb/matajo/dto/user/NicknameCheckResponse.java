package org.ktb.matajo.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NicknameCheckResponse {
    private boolean success;
    private String message;
    private NicknameData data;

    @Getter
    @AllArgsConstructor
    public static class NicknameData {
        private boolean available;
    }
}
