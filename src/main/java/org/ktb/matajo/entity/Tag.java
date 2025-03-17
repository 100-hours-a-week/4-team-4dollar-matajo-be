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
    @NonNull //필요하면 삭제
    @Column(nullable = false, length = 20, unique = true)
    private String tagName;

    @Builder.Default
    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL)
    private List<PostTag> postTagList = new ArrayList<>();
}