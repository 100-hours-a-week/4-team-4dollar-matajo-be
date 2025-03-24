package org.ktb.matajo.service.trade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.trade.TradeInfoListResponseDto;
import org.ktb.matajo.dto.trade.TradeInfoRequestDto;
import org.ktb.matajo.dto.trade.TradeInfoResponseDto;
import org.ktb.matajo.entity.ChatRoom;
import org.ktb.matajo.entity.MessageType;
import org.ktb.matajo.entity.Post;
import org.ktb.matajo.entity.TradeInfo;
import org.ktb.matajo.entity.User;
import org.ktb.matajo.global.error.code.ErrorCode;
import org.ktb.matajo.global.error.exception.BusinessException;
import org.ktb.matajo.repository.ChatRoomRepository;
import org.ktb.matajo.repository.TradeInfoRepository;
import org.ktb.matajo.repository.UserRepository;
import org.ktb.matajo.service.chat.ChatMessageService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TradeInfoServiceImpl implements TradeInfoService {
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final TradeInfoRepository tradeInfoRepository;

  @Override
  @Transactional
  public TradeInfoResponseDto createTrade(TradeInfoRequestDto tradeInfoRequestDto, Long userId) {
    // 사용자 존재 확인
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

    // 채팅방 존재 확인
    ChatRoom chatRoom =
        chatRoomRepository
            .findById(tradeInfoRequestDto.getRoomId())
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

    // 사용자가 채팅방의 참여자인지 확인 (보관인 또는 의뢰인)
    boolean isParticipant =
        chatRoom.getChatUserList().stream()
            .anyMatch(
                chatUser -> chatUser.getUser().getId().equals(userId) && chatUser.isActiveStatus());

    if (!isParticipant) {
      throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
    }

    // 거래 정보 생성
    TradeInfo tradeInfo =
        TradeInfo.builder()
            .productName(tradeInfoRequestDto.getProductName())
            .category(tradeInfoRequestDto.getCategory())
            .startDate(tradeInfoRequestDto.getStartDate())
            .storagePeriod(tradeInfoRequestDto.getStoragePeriod())
            .tradePrice(tradeInfoRequestDto.getTradePrice())
            .chatRoom(chatRoom)
            .build();

    // 데이터베이스에 저장
    TradeInfo savedTradeInfo = tradeInfoRepository.save(tradeInfo);

    log.info(
        "거래 정보 생성 완료: id={}, 상품명={}, 채팅방ID={}",
        savedTradeInfo.getId(),
        savedTradeInfo.getProductName(),
        chatRoom.getId());

        // 응답 DTO 변환 및 반환
        return TradeInfoResponseDto.builder()
                .tradeId(savedTradeInfo.getId())
                .build();
    }

  @Override
  public List<TradeInfoListResponseDto> getMyTrades(Long userId) {
    // 사용자 존재 확인
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    // 최적화된 쿼리로 한 번에 조회
    List<TradeInfo> tradeInfoList = tradeInfoRepository.findTradeInfoByUserId(userId);

    if (tradeInfoList.isEmpty()) {
      log.info("사용자 {}의 거래 내역이 없습니다.", userId);
      return new ArrayList<>();
    }

    // DTO로 변환
    List<TradeInfoListResponseDto> result =
        tradeInfoList.stream()
            .map(
                tradeInfo -> {
                  ChatRoom chatRoom = tradeInfo.getChatRoom();
                  Post post = chatRoom.getPost();

                  // 해당 거래에서 사용자가 보관인인지 확인
                  boolean keeperStatus = post.getUser().getId().equals(userId);

                  // 상대방 ID 설정
                  Long otherUserId =
                      keeperStatus ? chatRoom.getUser().getId() : post.getUser().getId();

                  return TradeInfoListResponseDto.builder()
                      .tradeId(tradeInfo.getId())
                      .keeperStatus(keeperStatus)
                      .productName(tradeInfo.getProductName())
                      .userId(otherUserId)
                      .postAddress(post.getAddress().getBname())
                      .tradeDate(
                          tradeInfo
                              .getTradeDate()
                              .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                      .startDate(
                          tradeInfo
                              .getStartDate()
                              .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                      .storagePeriod(tradeInfo.getStoragePeriod())
                      .tradePrice(tradeInfo.getTradePrice())
                      .build();
                })
            .collect(Collectors.toList());

    log.info("사용자 {}의 거래 내역 {}건을 조회했습니다.", userId, result.size());

    return result;
  }
}