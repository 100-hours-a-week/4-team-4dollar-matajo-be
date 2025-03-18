package org.ktb.matajo.service.chat;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ktb.matajo.dto.chat.ChatRoomCreateRequestDto;
import org.ktb.matajo.dto.chat.ChatRoomCreateResponseDto;
import org.ktb.matajo.dto.chat.ChatRoomResponseDto;
import org.ktb.matajo.entity.*;
import org.ktb.matajo.repository.ChatRoomRepository;
import org.ktb.matajo.repository.ChatUserRepository;
import org.ktb.matajo.repository.PostRepository;
import org.ktb.matajo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        Optional<ChatRoom> existingChatRoom = chatRoomRepository.findExistingChatRoom(userId, chatRoomCreateRequestDto.getPostId());

        if (existingChatRoom.isPresent()) {
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
                lastMessageTime = formatMessageTime(lastChatMessage.getCreatedAt());
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
     * 메시지 시간 포맷팅
     * 당일: HH:mm
     * 하루전: 어제
     * 그 이전: yyyy.MM.dd
     */
    private String formatMessageTime(LocalDateTime messageTime) {
        if (messageTime == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalDate messageDate = messageTime.toLocalDate();

        if (messageDate.equals(today)) {
            // 오늘 보낸 메시지는 HH:mm으로 표시
            return messageTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        } else if (messageDate.equals(today.minusDays(1))) {
            // 어제 보낸 메시지는 "어제"로 표시
            return "어제";
        } else {
            // 그 이외는 yyyy.MM.dd로 표시
            return messageTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        }
    }
}
