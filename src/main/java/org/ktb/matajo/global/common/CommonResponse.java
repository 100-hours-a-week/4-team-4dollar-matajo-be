package org.ktb.matajo.global.common;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CommonResponse<T>{
    private boolean success;
    private String message;
    private T data;

    public static <T> CommonResponse<T> success(String message, T data){
        return CommonResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> CommonResponse<T> error(String message){
        return CommonResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

}
