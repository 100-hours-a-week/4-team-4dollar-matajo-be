package org.ktb.matajo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(indexes = {
        @Index(name = "idx_post_tag_post_id", columnList = "post_id"),
        @Index(name = "idx_post_tag_tag_id", columnList = "tag_id")
})
public class PostTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    //글 id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT) , nullable = false)
    private Post post;

    //태그 id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private Tag tag;
}