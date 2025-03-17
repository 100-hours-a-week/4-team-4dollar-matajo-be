package org.ktb.matajo.global.error.code;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 400 BAD REQUEST
    INVALID_OFFSET_OR_LIMIT(HttpStatus.BAD_REQUEST, "invalid_offset_or_limit", "페이지네이션 파라미터가 유효하지 않습니다"),
    INVALID_POST_ID(HttpStatus.BAD_REQUEST, "invalid_post_id", "게시글 ID가 유효하지 않습니다"),
    INVALID_POST_TITLE(HttpStatus.BAD_REQUEST, "invalid_post_title", "게시글 제목이 유효하지 않습니다"),
    INVALID_POST_CONTENT(HttpStatus.BAD_REQUEST,  "invalid_post_content", "게시글 내용이 유효하지 않습니다"),
    INVALID_POST_ADDRESS(HttpStatus.BAD_REQUEST, "invalid_post_address", "게시글 주소가 유효하지 않습니다"),
    INVALID_POST_TAGS(HttpStatus.BAD_REQUEST, "invalid_post_tags", "게시글 태그가 유효하지 않습니다"),
    INVALID_POST_IMAGES(HttpStatus.BAD_REQUEST, "invalid_post_images", "게시글 이미지가 유효하지 않습니다"),
    INVALID_PREFER_PRICE(HttpStatus.BAD_REQUEST, "invalid_prefer_price", "선호 가격이 유효하지 않습니다"),

    // 401 UNAUTHORIZED
    REQUIRED_AUTHORIZATION(HttpStatus.UNAUTHORIZED, "required_authorization", "인증이 필요합니다"),

    // 403 FORBIDDEN
    REQUIRED_PERMISSION(HttpStatus.FORBIDDEN, "required_permission", "권한이 없습니다"),
    NO_PERMISSION_TO_UPDATE(HttpStatus.FORBIDDEN, "no_permission_to_update", "게시글 수정 권한이 없습니다"),
    NO_PERMISSION_TO_DELETE(HttpStatus.FORBIDDEN, "no_permission_to_delete", "게시글 삭제 권한이 없습니다"),

    // 404 NOT FOUND
    NOT_FOUND_POST(HttpStatus.NOT_FOUND, "not_found_post", "게시글을 찾을 수 없습니다"),
    NOT_FOUND_POSTS_PAGE(HttpStatus.NOT_FOUND, "not_found_posts_page", "해당 페이지에 게시글이 없습니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "user_not_found", "사용자를 찾을 수 없습니다"),

    // 500 INTERNAL SERVER ERROR
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "internal_server_error", "서버 내부 오류가 발생했습니다"),
    FAILED_TO_WRITE_POST(HttpStatus.INTERNAL_SERVER_ERROR, "failed_to_write_post", "게시글 작성에 실패했습니다"),
    FAILED_TO_DELETE_POST(HttpStatus.INTERNAL_SERVER_ERROR, "failed_to_delete_post", "게시글 삭제에 실패했습니다"),
    FAILED_TO_GET_POST_DETAIL(HttpStatus.INTERNAL_SERVER_ERROR,  "failed_to_get_post_detail", "게시글 상세 조회에 실패했습니다");

    private final HttpStatus status;
    private final String errorMessage;
    private final String description;

    ErrorCode(HttpStatus status, String errorMessage, String description) {
        this.status = status;
        this.errorMessage = errorMessage;
        this.description = description;
    }

}