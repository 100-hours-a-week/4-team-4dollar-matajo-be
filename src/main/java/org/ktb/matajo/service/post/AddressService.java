package org.ktb.matajo.service.post;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.post.AddressDto;
import org.ktb.matajo.entity.Address;
import org.ktb.matajo.global.error.code.ErrorCode;
import org.ktb.matajo.global.error.exception.BusinessException;
import org.ktb.matajo.repository.AddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    /**
     * 다음 주소 API에서 받은 전체 주소 정보를 저장하거나 기존 정보 조회
     */
    @Transactional
    public Address saveOrGetAddress(AddressDto addressDto) {
        // 유효성 검증
        if (addressDto == null || addressDto.getAddress() == null || addressDto.getAddress().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_POST_ADDRESS);
        }

        log.info("주소 정보 처리: {}", addressDto.getAddress());

        // 동일한 주소가 있는지 확인 (우편번호와 기본주소로 비교)
        Optional<Address> existingAddress = addressRepository.findByZonecodeAndAddress(
                addressDto.getZonecode(), addressDto.getAddress());

        if (existingAddress.isPresent()) {
            log.info("기존 주소 정보 재사용: ID={}", existingAddress.get().getId());
            return existingAddress.get();
        }

        // 새 주소 엔티티 생성 및 저장
        Address address = Address.builder()
                .postcode(addressDto.getPostcode())
                .postcode1(addressDto.getPostcode1())
                .postcode2(addressDto.getPostcode2())
                .postcodeSeq(addressDto.getPostcodeSeq())
                .zonecode(addressDto.getZonecode())
                .address(addressDto.getAddress())
                .addressEnglish(addressDto.getAddressEnglish())
                .addressType(addressDto.getAddressType())
                .bcode(addressDto.getBcode())
                .bname(addressDto.getBname())
                .bnameEnglish(addressDto.getBnameEnglish())
                .bname1(addressDto.getBname1())
                .bname1English(addressDto.getBname1English())
                .bname2(addressDto.getBname2())
                .bname2English(addressDto.getBname2English())
                .sido(addressDto.getSido())
                .sidoEnglish(addressDto.getSidoEnglish())
                .sigungu(addressDto.getSigungu())
                .sigunguEnglish(addressDto.getSigunguEnglish())
                .sigunguCode(addressDto.getSigunguCode())
                .userLanguageType(addressDto.getUserLanguageType())
                .query(addressDto.getQuery())
                .buildingName(addressDto.getBuildingName())
                .buildingCode(addressDto.getBuildingCode())
                .apartment(addressDto.getApartment())
                .jibunAddress(addressDto.getJibunAddress())
                .jibunAddressEnglish(addressDto.getJibunAddressEnglish())
                .roadAddress(addressDto.getRoadAddress())
                .roadAddressEnglish(addressDto.getRoadAddressEnglish())
                .autoRoadAddress(addressDto.getAutoRoadAddress())
                .autoRoadAddressEnglish(addressDto.getAutoRoadAddressEnglish())
                .autoJibunAddress(addressDto.getAutoJibunAddress())
                .autoJibunAddressEnglish(addressDto.getAutoJibunAddressEnglish())
                .userSelectedType(addressDto.getUserSelectedType())
                .noSelected(addressDto.getNoSelected())
                .hname(addressDto.getHname())
                .roadnameCode(addressDto.getRoadnameCode())
                .roadname(addressDto.getRoadname())
                .roadnameEnglish(addressDto.getRoadnameEnglish())
                .build();

        Address savedAddress = addressRepository.save(address);
        log.info("새 주소 정보 저장 완료: ID={}", savedAddress.getId());

        return savedAddress;
    }

    /**
     * 게시글 전용 주소 객체 생성 (일대일 관계 유지)
     */
    @Transactional
    public Address createAddressForPost(AddressDto addressDto) {
        // 유효성 검증
        if (addressDto == null || addressDto.getAddress() == null || addressDto.getAddress().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_POST_ADDRESS);
        }

        log.info("게시글용 주소 정보 생성: {}", addressDto.getAddress());

// 새 주소 엔티티 생성 및 저장
        Address address = Address.builder()
                .postcode(addressDto.getPostcode())
                .postcode1(addressDto.getPostcode1())
                .postcode2(addressDto.getPostcode2())
                .postcodeSeq(addressDto.getPostcodeSeq())
                .zonecode(addressDto.getZonecode())
                .address(addressDto.getAddress())
                .addressEnglish(addressDto.getAddressEnglish())
                .addressType(addressDto.getAddressType())
                .bcode(addressDto.getBcode())
                .bname(addressDto.getBname())
                .bnameEnglish(addressDto.getBnameEnglish())
                .bname1(addressDto.getBname1())
                .bname1English(addressDto.getBname1English())
                .bname2(addressDto.getBname2())
                .bname2English(addressDto.getBname2English())
                .sido(addressDto.getSido())
                .sidoEnglish(addressDto.getSidoEnglish())
                .sigungu(addressDto.getSigungu())
                .sigunguEnglish(addressDto.getSigunguEnglish())
                .sigunguCode(addressDto.getSigunguCode())
                .userLanguageType(addressDto.getUserLanguageType())
                .query(addressDto.getQuery())
                .buildingName(addressDto.getBuildingName())
                .buildingCode(addressDto.getBuildingCode())
                .apartment(addressDto.getApartment())
                .jibunAddress(addressDto.getJibunAddress())
                .jibunAddressEnglish(addressDto.getJibunAddressEnglish())
                .roadAddress(addressDto.getRoadAddress())
                .roadAddressEnglish(addressDto.getRoadAddressEnglish())
                .autoRoadAddress(addressDto.getAutoRoadAddress())
                .autoRoadAddressEnglish(addressDto.getAutoRoadAddressEnglish())
                .autoJibunAddress(addressDto.getAutoJibunAddress())
                .autoJibunAddressEnglish(addressDto.getAutoJibunAddressEnglish())
                .userSelectedType(addressDto.getUserSelectedType())
                .noSelected(addressDto.getNoSelected())
                .hname(addressDto.getHname())
                .roadnameCode(addressDto.getRoadnameCode())
                .roadname(addressDto.getRoadname())
                .roadnameEnglish(addressDto.getRoadnameEnglish())
                .build();

        Address savedAddress = addressRepository.save(address);
        log.info("게시글 전용 주소 정보 저장 완료: ID={}", savedAddress.getId());

        return savedAddress;
    }
}