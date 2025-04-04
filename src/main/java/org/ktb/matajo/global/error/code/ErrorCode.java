package org.ktb.matajo.global.error.code;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 400 BAD REQUEST
    // 구체적인 비즈니스 규칙 위반
    INVALID_OFFSET_OR_LIMIT(HttpStatus.BAD_REQUEST, "invalid_offset_or_limit", "페이지네이션 파라미터가 유효하지 않습니다"),
    INVALID_POST_ID(HttpStatus.BAD_REQUEST, "invalid_post_id", "게시글 ID가 유효하지 않습니다"),
    INVALID_POST_TITLE(HttpStatus.BAD_REQUEST, "invalid_post_title", "게시글 제목이 유효하지 않습니다"),
    INVALID_POST_CONTENT(HttpStatus.BAD_REQUEST,  "invalid_post_content", "게시글 내용이 유효하지 않습니다"),
    INVALID_POST_ADDRESS(HttpStatus.BAD_REQUEST, "invalid_post_address", "게시글 주소가 유효하지 않습니다"),
    INVALID_POST_TAGS(HttpStatus.BAD_REQUEST, "invalid_post_tags", "게시글 태그가 유효하지 않습니다"),
    INVALID_POST_IMAGES(HttpStatus.BAD_REQUEST, "invalid_post_images", "게시글 이미지가 유효하지 않습니다"),
    INVALID_PREFER_PRICE(HttpStatus.BAD_REQUEST, "invalid_prefer_price", "선호 가격이 유효하지 않습니다"),
    INVALID_LOCATION_ID(HttpStatus.BAD_REQUEST, "invalid_location_id", "올바르지 않은 위치 정보입니다"),
    INVALID_USER_ID(HttpStatus.BAD_REQUEST, "invalid_user_id", "사용자 ID가 유효하지 않습니다"),
    INVALID_CHAT_ROOM_ID(HttpStatus.BAD_REQUEST, "invalid_chat_room_id", "채팅방 ID가 유효하지 않습니다"),
    INVALID_IMAGE_CONTENT(HttpStatus.BAD_REQUEST, "invalid_image_content", "이미지 타입 메시지의 내용이 비어있습니다"),
    INVALID_IMAGE_URL(HttpStatus.BAD_REQUEST, "invalid_image_url", "이미지 URL이 유효하지 않습니다"),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "invalid_file_type", "파일 형식이 유효하지 않습니다"),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "file_size_exceeded", "파일 크기는 10MB 이하여야 합니다"),
    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "invalid_category", "이미지 카테고리가 유효하지 않습니다"),
    EMPTY_IMAGE(HttpStatus.BAD_REQUEST, "empty_image", "이미지 파일이 비어있거나 없습니다"),
    INVALID_FILE_NAME(HttpStatus.BAD_REQUEST, "invalid_file_name", "파일 이름이 유효하지 않습니다"),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "invalid_file_extension", "지원하지 않는 파일 확장자입니다. jpg, jpeg, png, bmp, webp, heic 형식만 업로드 가능합니다"),
    FILE_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "file_count_exceeded", "최대 업로드 가능한 파일 개수(4개)를 초과했습니다"),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "invalid_refresh_token", "리프레시 토큰이 유효하지 않습니다"),
    REQUIRED_AGREEMENT_MISSING(HttpStatus.BAD_REQUEST, "required_agreement_missing", "필수 약관에 동의하지 않았습니다"),
    NOTIFICATION_MESSAGE_INVALID(HttpStatus.BAD_REQUEST, "notification_message_invalid", "알림 메시지가 유효하지 않습니다"),
    NICKNAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "nickname_already_exists", "이미 사용 중인 닉네임입니다"),

    // 일반적인 입력값 검증 오류
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "invalid_input_value", "입력값이 유효하지 않습니다"),


    // 401 UNAUTHORIZED
    REQUIRED_AUTHORIZATION(HttpStatus.UNAUTHORIZED, "required_authorization", "인증이 필요합니다"),

    // 403 FORBIDDEN
    REQUIRED_PERMISSION(HttpStatus.FORBIDDEN, "required_permission", "권한이 없습니다"),
    NO_PERMISSION_TO_UPDATE(HttpStatus.FORBIDDEN, "no_permission_to_update", "게시글 수정 권한이 없습니다"),
    NO_PERMISSION_TO_DELETE(HttpStatus.FORBIDDEN, "no_permission_to_delete", "게시글 삭제 권한이 없습니다"),
    KEEPER_CANNOT_CREATE_CHATROOM(HttpStatus.FORBIDDEN, "keeper_cannot_create_chatroom", "보관인은 자신의 게시글에 채팅방을 생성할 수 없습니다"),

    // 404 NOT FOUND
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "not_found_post", "게시글을 찾을 수 없습니다"),
    POSTS_PAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "not_found_posts_page", "해당 페이지에 게시글이 없습니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "not_found_user", "사용자를 찾을 수 없습니다"),
    TAG_NAME_NOT_FOUND(HttpStatus.NOT_FOUND,"not_found_tag_name", "존재하지 않는 태그입니다."),
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "not_found_chat_room", "채팅방을 찾을 수 없습니다"),
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "not_found_chat_message", "채팅 메시지를 찾을 수 없습니다"),
    CHAT_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "not_found_chat_user", "채팅방 사용자를 찾을 수 없습니다"),
    POST_ALREADY_DELETED(HttpStatus.NOT_FOUND, "post_already_deleted", "이미 삭제된 게시글입니다"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "refresh_token_not_found", "리프레시 토큰이 존재하지 않습니다"),

    // 405 METHOD NOT ALLOWED - 누락된 에러 코드 추가
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "method_not_allowed", "지원하지 않는 HTTP 메소드입니다"),

    // 409 CONFLICT
    CHAT_USER_ALREADY_LEFT(HttpStatus.CONFLICT, "chat_user_already_left", "이미 채팅방을 나간 사용자입니다"),

    //429 TOO_MANY_REQUESTS
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS,"too_many_requests", "요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요."),

    // 500 INTERNAL SERVER ERROR
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "internal_server_error", "서버 내부 오류가 발생했습니다"),
    FAILED_TO_UPLOAD_IMAGE(HttpStatus.INTERNAL_SERVER_ERROR, "failed_to_upload_image", "이미지 업로드에 실패했습니다"),
    FAILED_TO_WRITE_POST(HttpStatus.INTERNAL_SERVER_ERROR, "failed_to_write_post", "게시글 작성에 실패했습니다"),
    FAILED_TO_DELETE_POST(HttpStatus.INTERNAL_SERVER_ERROR, "failed_to_delete_post", "게시글 삭제에 실패했습니다"),
    FAILED_TO_GET_POST_DETAIL(HttpStatus.INTERNAL_SERVER_ERROR,  "failed_to_get_post_detail", "게시글 상세 조회에 실패했습니다"),
    FAILED_TO_UPDATE_POST(HttpStatus.INTERNAL_SERVER_ERROR, "failed_to_update_post", "게시글 수정에 실패했습니다"),

    // 502 BAD GATEWAY - 외부 API 통신 실패
    KAKAO_AUTH_FAILED(HttpStatus.BAD_GATEWAY, "kakao_auth_failed", "카카오 인증에 실패했습니다"),
    KAKAO_USERINFO_FETCH_FAILED(HttpStatus.BAD_GATEWAY, "kakao_userinfo_failed", "카카오 사용자 정보 조회에 실패했습니다");

    private final HttpStatus status;
    private final String errorMessage;
    private final String description;

    ErrorCode(HttpStatus status, String errorMessage, String description) {
        this.status = status;
        this.errorMessage = errorMessage;
        this.description = description;
    }

}