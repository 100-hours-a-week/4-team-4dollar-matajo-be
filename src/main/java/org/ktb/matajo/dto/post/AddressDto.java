package org.ktb.matajo.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AddressDto {
    // 우편번호 관련 정보
    private String postcode;          // 구 우편번호
    private String postcode1;         // 구 우편번호 앞 3자리
    private String postcode2;         // 구 우편번호 뒤 3자리
    private String postcodeSeq;       // 구 우편번호 일련번호
    private String zonecode;          // 새 우편번호(5자리)

    // 기본 주소 정보
    private String address;           // 기본 주소
    private String addressEnglish;    // 영문 기본 주소
    private String addressType;       // 주소 타입(R: 도로명, J: 지번)

    // 법정동 정보
    private String bcode;             // 법정동 코드
    private String bname;             // 법정동/법정리 이름
    private String bnameEnglish;      // 법정동/법정리 이름 영문
    private String bname1;            // 법정리의 읍/면 이름
    private String bname1English;     // 법정리의 읍/면 이름 영문
    private String bname2;            // 법정동/법정리 이름
    private String bname2English;     // 법정동/법정리 이름 영문

    // 행정구역 정보
    private String sido;              // 도/시 이름
    private String sidoEnglish;       // 도/시 이름 영문
    private String sigungu;           // 시/군/구 이름
    private String sigunguEnglish;    // 시/군/구 이름 영문
    private String sigunguCode;       // 시/군/구 코드

    // 사용자 선택 정보
    private String userLanguageType;  // 사용자가 선택한 언어 (K: 한글, E: 영문)
    private String query;             // 사용자 검색어
    private String userSelectedType;  // 사용자가 선택한 주소 타입
    private String noSelected;        // 선택 안함 여부

    // 건물 정보
    private String buildingName;      // 건물명
    private String buildingCode;      // 건물관리번호
    private String apartment;         // 공동주택 여부 (Y/N)

    // 주소 전체 정보
    private String jibunAddress;      // 지번 주소
    private String jibunAddressEnglish; // 영문 지번 주소
    private String roadAddress;       // 도로명 주소
    private String roadAddressEnglish; // 영문 도로명 주소
    private String autoRoadAddress;   // 자동 매핑된 도로명 주소
    private String autoRoadAddressEnglish; // 자동 매핑된 영문 도로명 주소
    private String autoJibunAddress;  // 자동 매핑된 지번 주소
    private String autoJibunAddressEnglish; // 자동 매핑된 영문 지번 주소

    // 행정동 정보
    private String hname;             // 행정동 이름

    // 도로명 정보
    private String roadnameCode;      // 도로명 코드
    private String roadname;          // 도로명
    private String roadnameEnglish;   // 도로명 영문
}
