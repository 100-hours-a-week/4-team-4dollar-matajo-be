package org.ktb.matajo.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDto{
    private Long post_id;
    private String post_title;
    private String post_main_image;
    private String post_address;
    private int prefer_price;
    private List<String> post_tags;
}   