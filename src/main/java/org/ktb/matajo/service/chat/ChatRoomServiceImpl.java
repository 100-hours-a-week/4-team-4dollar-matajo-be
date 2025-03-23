package org.ktb.matajo.service.chat;

import static org.ktb.matajo.util.TimeFormatter.formatChatRoomTime;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    public ChatRoomCreateResponseDto createChatRoom(ChatRoomCreateRequestDto chatRoomCreateRequestDto, Long userId) {
        Post post = postRepository.findById(chatRoomCreateRequestDto.getPostId())
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        if (post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("보관인은 자신의 게시글에 채팅방을 생성할 수 없습니다.");
        }

        Optional<ChatRoom> existingChatRoom = chatRoomRepository.findByPostIdAndUserId(chatRoomCreateRequestDto.getPostId(), userId);

        if (existingChatRoom.isPresent()) {
            ChatRoom chatRoom = existingChatRoom.get();
            log.info("기존 채팅방 발견: roomId={}, postId={}, userId={}", chatRoom.getId(), post.getId(), userId);

            Optional<ChatUser> clientChatUser = chatUserRepository.findByChatRoomAndUserId(chatRoom, userId);

            if (clientChatUser.isPresent() && !clientChatUser.get().isActiveStatus()) {
                log.info("의뢰인 채팅방 재참여 처리: roomId={}, userId={}", chatRoom.getId(), userId);
                clientChatUser.get().rejoin();
            }

            return ChatRoomCreateResponseDto.builder().id(chatRoom.getId()).build();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        ChatRoom chatRoom = ChatRoom.builder().user(user).post(post).build();
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        ChatUser client = ChatUser.builder().chatRoom(savedChatRoom).user(user).activeStatus(true).build();
        ChatUser keeper = ChatUser.builder().chatRoom(savedChatRoom).user(post.getUser()).activeStatus(true).build();

        chatUserRepository.save(client);
        chatUserRepository.save(keeper);

        return ChatRoomCreateResponseDto.builder().id(savedChatRoom.getId()).build();
    }

    // ChatRoom → ChatRoomResponseDto 변환
    private ChatRoomResponseDto convertToChatRoomResponseDto(ChatRoom chatRoom, Long userId) {
        Post post = chatRoom.getPost();

        String otherUserNickname;
        boolean keeperStatus;

        if (post.getUser().getId().equals(userId)) {
            otherUserNickname = chatRoom.getUser().getNickname();
            keeperStatus = true;
        } else {
            otherUserNickname = post.getUser().getNickname();
            keeperStatus = false;
        }

        String postMainImage = post.getImageList().stream()
                .filter(Image::isThumbnailStatus)
                .findFirst()
                .map(Image::getImageUrl)
                .orElse("");

        String postAddress = post.getAddress().getBname();

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

    // 채팅방 리스트 조회
    @Override
    public List<ChatRoomResponseDto> getMyChatRooms(Long userId) {
        List<ChatUser> myChatUsers = chatUserRepository.findByUserIdAndActiveStatusIsTrue(userId);
        return myChatUsers.stream()
                .map(chatUser -> convertToChatRoomResponseDto(chatUser.getChatRoom(), userId))
                .collect(Collectors.toList());
    }

    // 채팅방 상세 정보 조회
    @Override
    public ChatRoomDetailResponseDto getChatRoomDetail(Long userId, Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

        boolean isMember = chatUserRepository.existsByUserIdAndChatRoomIdAndActiveStatusIsTrue(userId, roomId);
        if (!isMember) {
            throw new BusinessException(ErrorCode.REQUIRED_PERMISSION);
        }

        Post post = chatRoom.getPost();

        String postMainImage = post.getImageList().stream()
                .filter(Image::isThumbnailStatus)
                .findFirst()
                .map(Image::getImageUrl)
                .orElse("");

        String postAddress = post.getAddress() != null ? post.getAddress().getBname() : "";

        User keeper = post.getUser();
        User client = chatRoom.getUser();

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

    // 채팅방 나가기
    @Override
    @Transactional
    public void leaveChatRoom(Long userId, Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

        Long keeperId = chatRoom.getPost().getUser().getId();

        if (userId.equals(keeperId)) {
            throw new BusinessException(ErrorCode.REQUIRED_PERMISSION);
        }

        ChatUser chatUser = chatUserRepository.findByChatRoomAndUserId(chatRoom, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

        if (!chatUser.isActiveStatus()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        chatUser.leave();
        log.info("사용자 채팅방 나가기 처리: roomId={}, userId={}", roomId, userId);
    }
}
