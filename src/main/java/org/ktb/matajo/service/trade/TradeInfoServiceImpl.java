package org.ktb.matajo.service.trade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.chat.ChatMessageRequestDto;
import org.ktb.matajo.dto.chat.ChatMessageResponseDto;
import org.ktb.matajo.dto.trade.TradeInfoCurrentResponseDto;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TradeInfoServiceImpl implements TradeInfoService {
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final TradeInfoRepository tradeInfoRepository;
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate; // WebSocket 메시지 전송을 위한 템플릿 추가

    @Override
    @Transactional
    public TradeInfoResponseDto createTrade(TradeInfoRequestDto tradeInfoRequestDto, Long userId) {
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

        // 채팅방 존재 확인
        ChatRoom chatRoom = chatRoomRepository.findById(tradeInfoRequestDto.getRoomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

        // 사용자가 채팅방의 참여자인지 확인 (보관인 또는 의뢰인)
        boolean isParticipant = chatRoom.getChatUserList().stream()
                .anyMatch(chatUser -> chatUser.getUser().getId().equals(userId) && chatUser.isActiveStatus());

        if (!isParticipant) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 거래 정보 생성
        TradeInfo tradeInfo = TradeInfo.builder()
                .productName(tradeInfoRequestDto.getProductName())
                .category(tradeInfoRequestDto.getCategory())
                .startDate(tradeInfoRequestDto.getStartDate())
                .storagePeriod(tradeInfoRequestDto.getStoragePeriod())
                .tradePrice(tradeInfoRequestDto.getTradePrice())
                .chatRoom(chatRoom)
                .build();

        // 데이터베이스에 저장
        TradeInfo savedTradeInfo = tradeInfoRepository.save(tradeInfo);

        log.info("거래 정보 생성 완료: id={}, 상품명={}, 채팅방ID={}",
                savedTradeInfo.getId(), savedTradeInfo.getProductName(), chatRoom.getId());

        // 거래 확정 메시지를 채팅방에 자동으로 전송
        // 메시지 전송 실패가 거래 생성 자체를 실패시키지 않도록 별도 메서드에서 처리
        sendTradeConfirmationMessage(chatRoom.getId(), userId);

        // 응답 DTO 변환 및 반환
        return TradeInfoResponseDto.builder()
                .tradeId(savedTradeInfo.getId())
                .build();
    }

    /**
     * 거래 확정 메시지를 채팅방에 전송하는 메서드
     *
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID (메시지 발신자)
     */
    private void sendTradeConfirmationMessage(Long roomId, Long userId) {
        // "거래가 확정되었습니다." 메시지 생성
        ChatMessageRequestDto messageDto = ChatMessageRequestDto.builder()
                .senderId(userId)
                .content("거래가 확정 되었습니다.\n거래 내역을 확인해주세요.")
                .messageType(MessageType.SYSTEM)
                .build();

        // 채팅 메시지 서비스를 통해 메시지 전송
        try {
            ChatMessageResponseDto chatMessageResponseDto = chatMessageService.saveMessage(roomId, messageDto);
            log.info("거래 확정 메시지 전송 완료: roomId={}, userId={}", roomId, userId);

            // WebSocket을 통해 메시지 브로드캐스트 - 실시간 전송을 위한 핵심 코드
            messagingTemplate.convertAndSend("/topic/chat/" + roomId, chatMessageResponseDto);
            log.info("거래 확정 메시지 실시간 전송 완료: roomId={}", roomId);
        } catch (BusinessException e) {
            // 비즈니스 예외 발생 시 로그만 남기고 진행 (거래 생성은 성공해야 함)
            log.error("거래 확정 메시지 전송 중 비즈니스 예외 발생: {}", e.getMessage(), e);
        } catch (Exception e) {
            // 기타 예외 발생 시 로그만 남기고 진행
            log.error("거래 확정 메시지 전송 실패: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<TradeInfoListResponseDto> getMyTrades(Long userId) {
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 최적화된 쿼리로 한 번에 조회
        List<TradeInfo> tradeInfoList = tradeInfoRepository.findTradeInfoByUserId(userId);

        if (tradeInfoList.isEmpty()) {
            log.info("사용자 {}의 거래 내역이 없습니다.", userId);
            return new ArrayList<>();
        }

        // DTO로 변환
        List<TradeInfoListResponseDto> result = tradeInfoList.stream()
                .map(tradeInfo -> {
                    ChatRoom chatRoom = tradeInfo.getChatRoom();
                    Post post = chatRoom.getPost();

                    // 해당 거래에서 사용자가 보관인인지 확인
                    boolean keeperStatus = post.getUser().getId().equals(userId);

                    // 상대방 ID 설정
                    User otherUser = keeperStatus ?
                            chatRoom.getUser() : post.getUser();

                    return TradeInfoListResponseDto.builder()
                            .tradeId(tradeInfo.getId())
                            .keeperStatus(keeperStatus)
                            .productName(tradeInfo.getProductName())
                            .userId(otherUser.getId())
                            .nickname(otherUser.getNickname())
                            .postAddress(post.getAddress().getBname())
                            .tradeDate(tradeInfo.getTradeDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                            .startDate(tradeInfo.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                            .storagePeriod(tradeInfo.getStoragePeriod())
                            .tradePrice(tradeInfo.getTradePrice())
                            .dailyPrice(tradeInfo.getTradePrice() / tradeInfo.getStoragePeriod())
                            .build();
                })
                .collect(Collectors.toList());

        log.info("사용자 {}의 거래 내역 {}건을 조회했습니다.", userId, result.size());

        return result;
    }

    @Override
    public List<TradeInfoCurrentResponseDto> getCurrentTrades(Long localInfoId) {
        Pageable pageable = PageRequest.of(0, 2);
        List<TradeInfo> currentTradeInfoList = tradeInfoRepository.findTop2ByLocationId(localInfoId, pageable);

        // `TradeInfo` 리스트를 `TradeInfoResponseDTO` 리스트로 매핑
        return currentTradeInfoList.stream().map(tradeInfo -> {
            String mainImage = tradeInfo.getChatRoom().getPost().getImageList().stream()
                    .filter(image -> image.isThumbnailStatus() == true)  // 메인 이미지만 선택
                    .map(image -> image.getImageUrl())
                    .findFirst()
                    .orElse(null);  // 메인 이미지가 없으면 null 반환

            return new TradeInfoCurrentResponseDto(
                    mainImage,
                    tradeInfo.getProductName(),
                    tradeInfo.getCategory(),
                    tradeInfo.getTradeDate(),
                    tradeInfo.getTradePrice(),
                    tradeInfo.getStoragePeriod()
            );
        }).collect(Collectors.toList());
    }
}