package org.ktb.matajo.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 채팅 관련 시간 포맷팅 유틸리티 클래스
 */
public class TimeFormatter {

    /**
     * 채팅방 리스트에서 표시할 시간 포맷
     * - 오늘: HH:mm
     * - 어제: 어제
     * - 그 외: yyyy.MM.dd
     */
    public static String formatChatRoomTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        LocalDate today = LocalDate.now();
        LocalDate messageDate = dateTime.toLocalDate();

        if (messageDate.equals(today)) {
            // 오늘
            return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        } else if (messageDate.equals(today.minusDays(1))) {
            // 어제
            return "어제";
        } else {
            // 그 외
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        }
    }

    /**
     * 메시지 상세 화면에서 표시할 시간 포맷
     * - HH:mm 형식
     */
    public static String formatChatMessageTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}