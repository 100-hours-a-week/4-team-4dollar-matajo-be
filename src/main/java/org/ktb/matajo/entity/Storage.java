package org.ktb.matajo.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "storage")
@Getter
public class Storage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String name;

    @Column(columnDefinition = "text")
    private String address;

    private Double x;
    private Double y;

    @Column(length = 255)
    private String category;

    @Column(length = 255)
    private String keyword;

    @Column(length = 100)
    private String region;

    @Column(length = 50)
    private String phone;

    @Column(name = "place_id")
    private Long placeId;

    @Column(name = "kakao_map_link", columnDefinition = "text")
    private String kakaoMapLink;

    private Integer zonecode; // int 타입

    @Column(length = 100)
    private String bname2;

    @Column(length = 100)
    private String sigungu;

    @Column(length = 100)
    private String sido;

    @Column(name = "location_info_id")
    private Long locationInfoId;
}
