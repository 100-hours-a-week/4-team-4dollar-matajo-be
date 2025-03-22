package org.ktb.matajo.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import org.ktb.matajo.entity.Address;

/**
 * 위치 기반 게시글 정보 응답 DTO
 * Post ID와 Address 정보 포함
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LocationResponseDto {
    private Long postId;
    private String address;
    //private Address address;
}
