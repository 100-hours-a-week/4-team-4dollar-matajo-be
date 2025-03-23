package org.ktb.matajo.service.chat;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.chat.ChatRoomCreateRequestDto;
import org.ktb.matajo.dto.chat.ChatRoomCreateResponseDto;
import org.ktb.matajo.dto.chat.ChatRoomDetailResponseDto;
import org.ktb.matajo.dto.chat.ChatRoomResponseDto;
import org.ktb.matajo.entity.*;
import org.ktb.matajo.global.error.code.ErrorCode;
import org.ktb.matajo.global.error.exception.BusinessException;
import org.ktb.matajo.repository.ChatRoomRepository;
import org.ktb.matajo.repository.ChatUserRepository;
import org.ktb.matajo.repository.PostRepository;
import org.ktb.matajo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.ktb.matajo.util.TimeFormatter.formatChatRoomTime;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomServiceImpl implements ChatRoomService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatUserRepository chatUserRepository;

    // 채팅방 생성
    @Override
    @Transactional
    public ChatRoomCreateResponseDto createChatRoom(
            ChatRoomCreateRequestDto chatRoomCreateRequestDto, Long userId) {

        // 게시글 조회(에러는 추후 수정)
        Post post = postRepository.findById(chatRoomCreateRequestDto.getPostId())
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        // 보관인이 자신의 게시글에 채팅방을 생성하는 경우 방지
        if (post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("보관인은 자신의 게시글에 채팅방을 생성할 수 없습니다.");
        }

        // 채팅방이 있는지 확인하기(의뢰인ID 및 게시글ID)
        Optional<ChatRoom> existingChatRoom = chatRoomRepository.findByPostIdAndUserId(chatRoomCreateRequestDto.getPostId(), userId);

        if (existingChatRoom.isPresent()) {
            ChatRoom chatRoom = existingChatRoom.get();
            log.info("기존 채팅방 발견: roomId={}, postId={}, userId={}",
                    chatRoom.getId(), post.getId(), userId);

            // 채팅방에 참여한 의뢰인 정보 조회
            Optional<ChatUser> clientChatUser = chatUserRepository.findByChatRoomAndUserId(chatRoom, userId);

            if (clientChatUser.isPresent() && !clientChatUser.get().isActiveStatus()) {
                log.info("의뢰인 채팅방 재참여 처리: roomId={}, userId={}", chatRoom.getId(), userId);
                clientChatUser.get().rejoin();
            }

            return ChatRoomCreateResponseDto.builder()
                    .id(existingChatRoom.get().getId())
                    .build();
        }

        // 현재 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 새 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .user(user)
                .post(post)
                .build();

        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        // 채팅 참여자 추가 (의뢰인)
        ChatUser client = ChatUser.builder()
                .chatRoom(savedChatRoom)
                .user(user)
                .activeStatus(true)
                .build();

        // 채팅 참여자 추가 (보관인)
        ChatUser keeper = ChatUser.builder()
                .chatRoom(savedChatRoom)
                .user(post.getUser())
                .activeStatus(true)
                .build();

        chatUserRepository.save(client);
        chatUserRepository.save(keeper);

        return ChatRoomCreateResponseDto.builder()
                .id(savedChatRoom.getId())
                .build();
    }

    // ChatRoom 엔티티를 ChatRoomResponseDto로 변환
    private ChatRoomResponseDto convertToChatRoomResponseDto(ChatRoom chatRoom, Long userId) {
        Post post = chatRoom.getPost();

        // 상대방 정보 가져오기
        String otherUserNickname;
        boolean keeperStatus;

        if(post.getUser().getId().equals(userId)) {
            // 사용자가 보관인인 경우
            otherUserNickname = chatRoom.getUser().getNickname();
            keeperStatus = true;
        } else {
            // 사용자가 의뢰인인 경우
            otherUserNickname = post.getUser().getNickname();
            keeperStatus = false;
        }

        // 게시글 메인 이미지 URL 가져오기
        String postMainImage = post.getImageList().stream()
                .filter(Image:: isThumbnailStatus)
                .findFirst()
                .map(Image::getImageUrl)
                .orElse("");

        // 게시글 주소 가져오기 (도시 -> 동, 시골 -> 리)
        String postAddress = post.getAddress().getBname();

        // 마지막 메시지 정보
        String lastMessage = "";
        String lastMessageTime = "";

        if (!chatRoom.getChatMessageList().isEmpty()) {
            ChatMessage lastChatMessage = chatRoom.getChatMessageList().stream()
                    .max(Comparator.comparing(ChatMessage::getCreatedAt))
                    .orElse(null);

            if (lastChatMessage != null) {
                lastMessage = lastChatMessage.getContent();
                lastMessageTime = formatChatRoomTime(lastChatMessage.getCreatedAt());
            }
        }

        return ChatRoomResponseDto.builder()
                .chatRoomId(chatRoom.getId())
                .keeperStatus(keeperStatus)
                .userNickname(otherUserNickname)
                .postMainImage(postMainImage)
                .postAddress(postAddress)
                .lastMessage(lastMessage)
                .lastMessageTime(lastMessageTime)
                .build();
    }

    // 채팅방 리스트
    @Override
    public List<ChatRoomResponseDto> getMyChatRooms(Long userId) {
        List<ChatUser> myChatUsers = chatUserRepository.findByUserIdAndActiveStatusIsTrue(userId);

        return myChatUsers.stream()
                .map(chatUser -> convertToChatRoomResponseDto(chatUser.getChatRoom(), userId))
                .collect(Collectors.toList());
    }

    /**
     * 채팅방 상세 정보 조회
     */
    @Override
    public ChatRoomDetailResponseDto getChatRoomDetail(Long userId, Long roomId) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

        // 사용자가 채팅방 참여자인지 확인
        boolean isMember = chatUserRepository.existsByUserIdAndChatRoomIdAndActiveStatusIsTrue(userId, roomId);
        if (!isMember) {
            throw new BusinessException(ErrorCode.REQUIRED_PERMISSION);
        }

        // 게시글 정보
        Post post = chatRoom.getPost();

        // 썸네일 이미지 URL 가져오기
        String postMainImage = post.getImageList().stream()
                .filter(Image::isThumbnailStatus)
                .findFirst()
                .map(Image::getImageUrl)
                .orElse("");

        // 주소 정보 (null 체크 추가)
        String postAddress = "";
        if (post.getAddress() != null) {
            postAddress = post.getAddress().getBname();
        }

        // 보관인 정보 (게시글 작성자)
        User keeper = post.getUser();

        // 의뢰인 정보 (채팅방 생성자)
        User client = chatRoom.getUser();

        // 채팅방 상세 정보 DTO 생성
        return ChatRoomDetailResponseDto.builder()
                .roomId(chatRoom.getId())
                .postId(post.getId())
                .postTitle(post.getTitle())
                .postMainImage(postMainImage)
                .postAddress(postAddress)
                .preferPrice(post.getPreferPrice())
                .keeperId(keeper.getId())
                .keeperNickname(keeper.getNickname())
                .clientId(client.getId())
                .clientNickname(client.getNickname())
                .build();
    }

    @Override
    @Transactional
    public void leaveChatRoom(Long userId, Long roomId) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

        // 보관인 확인
        Long keeperId = chatRoom.getPost().getUser().getId();

        // 보관인이 나가려고 하면 예외 발생
        if (userId.equals(keeperId)) {
            throw new BusinessException(ErrorCode.REQUIRED_PERMISSION);
        }

        // 채팅방의 사용자 참여 정보 조회
        ChatUser chatUser = chatUserRepository.findByChatRoomAndUserId(chatRoom, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

        if(!chatUser.isActiveStatus()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 채팅방 나가기
        chatUser.leave();
        log.info("사용자 채팅방 나가기 처리: roomId={}, userId={}", roomId, userId);
    }

}
