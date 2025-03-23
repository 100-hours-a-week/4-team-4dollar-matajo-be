package org.ktb.matajo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(indexes = {
                @Index(name = "idx_location_original_name", columnList = "original_name"),
                @Index(name = "idx_location_city_district", columnList = "city_district")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class LocationInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_name", length = 10)
    private String originalName; // 동 이름

    @Column(name = "formatted_address", length = 255)
    private String formattedAddress; // 포맷 주소

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;  // 위도

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;  // 경도

    @Column(name = "display_name", columnDefinition = "TEXT")
    private String displayName;  // 표시 이름

    @Column(name = "class", length = 20)
    private String clazz;  // 클래스 (class는 예약어)

    @Column(length = 20)
    private String type;  // 타입

    @Column(name = "city_district", length = 50)
    private String cityDistrict;  // 시군구 (DB에 추가 필요)

    // Address 엔티티와의 양방향 관계 설정
    // @OneToMany(mappedBy = "locationInfo")
    // private List<Address> addresses = new ArrayList<>();


}
