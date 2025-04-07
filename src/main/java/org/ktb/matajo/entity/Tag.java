package org.ktb.matajo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    //태그 이름
    @Column(nullable = false, length = 20, unique = true)
    private String tagName;
    
    //태그 카테고리
    @Column(nullable = false)
    private Long tagCategoryId;

    @Builder.Default
    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL)
    private List<PostTag> postTagList = new ArrayList<>();
}