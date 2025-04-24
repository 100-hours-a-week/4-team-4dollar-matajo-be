package org.ktb.matajo.dto.storage;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StorageResponseDto {

    private Long id;

    @JsonProperty("kakao_map_link")
    private String kakaoMapLink;

    private String name;

    @JsonProperty("location_info_id")
    private Long locationInfoId;
}
