package org.ktb.matajo.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Schema(description = "주소 정보 DTO (다음 주소 API 응답 구조)")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AddressDto {
    // 우편번호 관련 정보
    @Schema(description = "구 우편번호", example = "123456")
    private String postcode;

    @Schema(description = "구 우편번호 앞 3자리", example = "123")
    private String postcode1;

    @Schema(description = "구 우편번호 뒤 3자리", example = "456")
    private String postcode2;

    @Schema(description = "구 우편번호 일련번호", example = "1")
    private String postcodeSeq;

    @Schema(description = "새 우편번호(5자리)", example = "12345")
    private String zonecode;

    // 기본 주소 정보
    @Schema(description = "기본 주소", example = "서울특별시 강남구 테헤란로 123")
    private String address;

    @Schema(description = "영문 기본 주소", example = "123 Teheran-ro, Gangnam-gu, Seoul")
    private String addressEnglish;

    @Schema(description = "주소 타입(R: 도로명, J: 지번)", example = "R")
    private String addressType;

    // 법정동 정보
    @Schema(description = "법정동 코드", example = "1168010100")
    private String bcode;

    @Schema(description = "법정동/법정리 이름", example = "삼성동")
    private String bname;

    @Schema(description = "법정동/법정리 이름 영문", example = "Samseong-dong")
    private String bnameEnglish;

    @Schema(description = "법정리의 읍/면 이름", example = "")
    private String bname1;

    @Schema(description = "법정리의 읍/면 이름 영문", example = "")
    private String bname1English;

    @Schema(description = "법정동/법정리 이름", example = "삼성동")
    private String bname2;

    @Schema(description = "법정동/법정리 이름 영문", example = "Samseong-dong")
    private String bname2English;

    // 행정구역 정보
    @Schema(description = "도/시 이름", example = "서울")
    private String sido;

    @Schema(description = "도/시 이름 영문", example = "Seoul")
    private String sidoEnglish;

    @Schema(description = "시/군/구 이름", example = "강남구")
    private String sigungu;

    @Schema(description = "시/군/구 이름 영문", example = "Gangnam-gu")
    private String sigunguEnglish;

    @Schema(description = "시/군/구 코드", example = "11680")
    private String sigunguCode;

    // 사용자 선택 정보
    @Schema(description = "사용자가 선택한 언어 (K: 한글, E: 영문)", example = "K")
    private String userLanguageType;

    @Schema(description = "사용자 검색어", example = "삼성동")
    private String query;

    @Schema(description = "사용자가 선택한 주소 타입", example = "R")
    private String userSelectedType;

    @Schema(description = "선택 안함 여부", example = "N")
    private String noSelected;

    // 건물 정보
    @Schema(description = "건물명", example = "삼성타워")
    private String buildingName;

    @Schema(description = "건물관리번호", example = "1234567890")
    private String buildingCode;

    @Schema(description = "공동주택 여부 (Y/N)", example = "N")
    private String apartment;

    // 주소 전체 정보
    @Schema(description = "지번 주소", example = "서울특별시 강남구 삼성동 123-45")
    private String jibunAddress;

    @Schema(description = "영문 지번 주소", example = "123-45 Samseong-dong, Gangnam-gu, Seoul")
    private String jibunAddressEnglish;

    @Schema(description = "도로명 주소", example = "서울특별시 강남구 테헤란로 123")
    private String roadAddress;

    @Schema(description = "영문 도로명 주소", example = "123 Teheran-ro, Gangnam-gu, Seoul")
    private String roadAddressEnglish;

    @Schema(description = "자동 매핑된 도로명 주소", example = "서울특별시 강남구 테헤란로 123")
    private String autoRoadAddress;

    @Schema(description = "자동 매핑된 영문 도로명 주소", example = "123 Teheran-ro, Gangnam-gu, Seoul")
    private String autoRoadAddressEnglish;

    @Schema(description = "자동 매핑된 지번 주소", example = "서울특별시 강남구 삼성동 123-45")
    private String autoJibunAddress;

    @Schema(description = "자동 매핑된 영문 지번 주소", example = "123-45 Samseong-dong, Gangnam-gu, Seoul")
    private String autoJibunAddressEnglish;

    // 행정동 정보
    @Schema(description = "행정동 이름", example = "삼성1동")
    private String hname;

    // 도로명 정보
    @Schema(description = "도로명 코드", example = "4146010")
    private String roadnameCode;

    @Schema(description = "도로명", example = "테헤란로")
    private String roadname;

    @Schema(description = "도로명 영문", example = "Teheran-ro")
    private String roadnameEnglish;
}