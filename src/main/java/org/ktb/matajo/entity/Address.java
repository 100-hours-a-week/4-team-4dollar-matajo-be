package org.ktb.matajo.entity;

import org.ktb.matajo.dto.post.AddressDto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class Address {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  private Long id;

  @Column(length = 10)
  private String postcode;

  @Column(length = 10)
  private String postcode1;

  @Column(length = 10)
  private String postcode2;

  @Column(length = 10)
  private String postcodeSeq;

  @Column(length = 10)
  private String zonecode;

  @Column(length = 200)
  private String address;

  @Column(length = 200)
  private String addressEnglish;

  @Column(length = 10)
  private String addressType;

  @Column(length = 20)
  private String bcode;

  @Column(length = 100)
  private String bname;

  @Column(length = 100)
  private String bnameEnglish;

  @Column(length = 100)
  private String bname1;

  @Column(name = "bname1_english", length = 100)
  private String bname1English;

  @Column(length = 100)
  private String bname2;

  @Column(name = "bname2_english", length = 100)
  private String bname2English;

  @Column(length = 50)
  private String sido;

  @Column(length = 50)
  private String sidoEnglish;

  @Column(length = 50)
  private String sigungu;

  @Column(length = 50)
  private String sigunguEnglish;

  @Column(length = 20)
  private String sigunguCode;

  @Column(length = 10)
  private String userLanguageType;

  @Column(length = 100)
  private String query;

  @Column(length = 100)
  private String buildingName;

  @Column(length = 50)
  private String buildingCode;

  @Column(length = 5)
  private String apartment;

  @Column(length = 200)
  private String jibunAddress;

  @Column(length = 200)
  private String jibunAddressEnglish;

  @Column(length = 200)
  private String roadAddress;

  @Column(length = 200)
  private String roadAddressEnglish;

  @Column(length = 200)
  private String autoRoadAddress;

  @Column(length = 200)
  private String autoRoadAddressEnglish;

  @Column(length = 200)
  private String autoJibunAddress;

  @Column(length = 200)
  private String autoJibunAddressEnglish;

  @Column(length = 10)
  private String userSelectedType;

  @Column(length = 5)
  private String noSelected;

  @Column(length = 100)
  private String hname;

  @Column(length = 20)
  private String roadnameCode;

  @Column(length = 50)
  private String roadname;

  @Column(length = 50)
  private String roadnameEnglish;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "location_info_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
  private LocationInfo locationInfo;

  public void update(AddressDto addressDto) {
    this.postcode = addressDto.getPostcode();
    this.postcode1 = addressDto.getPostcode1();
    this.postcode2 = addressDto.getPostcode2();
    this.postcodeSeq = addressDto.getPostcodeSeq();
    this.zonecode = addressDto.getZonecode();
    this.address = addressDto.getAddress();
    this.addressEnglish = addressDto.getAddressEnglish();
    this.addressType = addressDto.getAddressType();
    this.bcode = addressDto.getBcode();
    this.bname = addressDto.getBname();
    this.bnameEnglish = addressDto.getBnameEnglish();
    this.bname1 = addressDto.getBname1();
    this.bname1English = addressDto.getBname1English();
    this.bname2 = addressDto.getBname2();
    this.bname2English = addressDto.getBname2English();
    this.sido = addressDto.getSido();
    this.sidoEnglish = addressDto.getSidoEnglish();
    this.sigungu = addressDto.getSigungu();
    this.sigunguEnglish = addressDto.getSigunguEnglish();
    this.sigunguCode = addressDto.getSigunguCode();
    this.userLanguageType = addressDto.getUserLanguageType();
    this.query = addressDto.getQuery();
    this.buildingName = addressDto.getBuildingName();
    this.buildingCode = addressDto.getBuildingCode();
    this.apartment = addressDto.getApartment();
    this.jibunAddress = addressDto.getJibunAddress();
    this.jibunAddressEnglish = addressDto.getJibunAddressEnglish();
    this.roadAddress = addressDto.getRoadAddress();
    this.roadAddressEnglish = addressDto.getRoadAddressEnglish();
    this.autoRoadAddress = addressDto.getAutoRoadAddress();
    this.autoRoadAddressEnglish = addressDto.getAutoRoadAddressEnglish();
    this.autoJibunAddress = addressDto.getAutoJibunAddress();
    this.autoJibunAddressEnglish = addressDto.getAutoJibunAddressEnglish();
    this.userSelectedType = addressDto.getUserSelectedType();
    this.noSelected = addressDto.getNoSelected();
    this.hname = addressDto.getHname();
    this.roadnameCode = addressDto.getRoadnameCode();
    this.roadname = addressDto.getRoadname();
    this.roadnameEnglish = addressDto.getRoadnameEnglish();
  }

  public void updateLocationInfo(LocationInfo locationInfo) {
    this.locationInfo = locationInfo;
  }
}
