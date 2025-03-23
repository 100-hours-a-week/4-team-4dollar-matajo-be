package org.ktb.matajo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.ktb.matajo.entity.common.BaseEntity;

@Entity
@Getter
@Builder
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RefreshToken extends BaseEntity {
    @Id
    private Long userId;

    @Column(nullable = false, unique = true)
    private String token;

    public void updateToken(String newToken) {
        this.token = newToken;
    }
}
