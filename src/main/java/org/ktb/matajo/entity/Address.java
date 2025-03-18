package org.ktb.matajo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.ktb.matajo.entity.common.BaseEntity;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class Address extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;  // 자동 증가하는 기본키

    @Column(length = 10)
    private String postcode;  // 구 우편번호 (2020년 3월 9일 이후로는 데이터가 내려가지 않습니다.)

    @Column(length = 10)
    private String postcode1;  // 구 우편번호 앞 3자리 (2020년 3월 9일 이후로는 데이터가 내려가지 않습니다.)

    @Column(length = 10)
    private String postcode2;  // 구 우편번호 뒤 3자리 (2020년 3월 9일 이후로는 데이터가 내려가지 않습니다.)

    @Column(length = 10)
    private String postcodeSeq;  // 구 우편번호 일련번호 (2020년 3월 9일 이후로는 데이터가 내려가지 않습니다.)

    @Column(length = 10)
    private String zonecode;  // 국가기초구역번호. 2015년 8월 1일부터 시행될 새 우편번호.

    @Column(length = 200)
    private String address;  // 기본 주소 (검색 결과에서 첫줄에 나오는 주소, 검색어의 타입(지번/도로명)에 따라 달라집니다.)

    @Column(length = 200)
    private String addressEnglish;  // 기본 영문 주소

    @Column(length = 10)
    private String addressType;  // 검색된 기본 주소 타입: R(도로명), J(지번)

    @Column(length = 20)
    private String bcode;  // 법정동/법정리 코드

    @Column(length = 100)
    private String bname;  // 법정동/법정리 이름

    @Column(length = 100)
    private String bnameEnglish;  // 법정동/법정리 이름의 영문

    @Column(length = 100)
    private String bname1;  // 법정리의 읍/면 이름 ("동"지역일 경우에는 공백, "리"지역일 경우에는 "읍" 또는 "면" 정보가 들어갑니다.)

    @Column(name = "bname1_english", length = 100)
    private String bname1English;  // 법정리의 읍/면 이름의 영문 ("동"지역일 경우에는 공백, "리"지역일 경우에는 "읍" 또는 "면" 정보가 들어갑니다.)

    @Column(length = 100)
    private String bname2;  // 법정동/법정리 이름

    @Column(name = "bname2_english", length = 100)
    private String bname2English;  // 법정동/법정리 이름의 영문

    @Column(length = 50)
    private String sido;  // 도/시 이름

    @Column(length = 50)
    private String sidoEnglish;  // 도/시 이름의 영문

    @Column(length = 50)
    private String sigungu;  // 시/군/구 이름

    @Column(length = 50)
    private String sigunguEnglish;  // 시/군/구 이름의 영문

    @Column(length = 20)
    private String sigunguCode;  // 시/군/구 코드 (5자리 구성된 시/군/구 코드입니다.)

    @Column(length = 10)
    private String userLanguageType;  // 검색 결과에서 사용자가 선택한 주소의 언어 타입: K(한글주소), E(영문주소)

    @Column(length = 100)
    private String query;  // 사용자가 입력한 검색어

    @Column(length = 100)
    private String buildingName;  // 건물명

    @Column(length = 50)
    private String buildingCode;  // 건물관리번호

    @Column(length = 5)
    private String apartment;  // 공동주택 여부 (아파트,연립주택,다세대주택 등)

    @Column(length = 200)
    private String jibunAddress;  // 지번 주소 (도로명:지번 주소가 1:N인 경우에는 데이터가 공백으로 들어갈 수 있습니다.)

    @Column(length = 200)
    private String jibunAddressEnglish;  // 영문 지번 주소

    @Column(length = 200)
    private String roadAddress;  // 도로명 주소 (지번:도로명 주소가 1:N인 경우에는 데이터가 공백으로 들어갈 수 있습니다.)

    @Column(length = 200)
    private String roadAddressEnglish;  // 영문 도로명 주소

    @Column(length = 200)
    private String autoRoadAddress;  // '지번주소'에 매핑된 '도로명주소'가 여러개인 경우의 첫번째 매핑 주소

    @Column(length = 200)
    private String autoRoadAddressEnglish;  // autoRoadAddress의 영문 도로명 주소

    @Column(length = 200)
    private String autoJibunAddress;  // '도로명주소'에 매핑된 '지번주소'가 여러개인 경우의 첫번째 매핑 주소

    @Column(length = 200)
    private String autoJibunAddressEnglish;  // autoJibunAddress의 영문 지번 주소

    @Column(length = 10)
    private String userSelectedType;  // 검색 결과에서 사용자가 선택한 주소의 타입

    @Column(length = 5)
    private String noSelected;  // 연관 주소에서 "선택 안함" 부분을 선택했을때를 구분할 수 있는 상태변수

    @Column(length = 100)
    private String hname;  // 행정동 이름

    @Column(length = 20)
    private String roadnameCode;  // 도로명 코드

    @Column(length = 50)
    private String roadname;  // 도로명 값

    @Column(length = 50)
    private String roadnameEnglish;  // 도로명의 영문 값
}