package com.example.demo.dto;

import java.time.LocalDateTime;

public record Notice(
        Integer noticeId,
        String title,
        String url,
        String content,
        String department,
        String keyword,
        LocalDateTime crawledAt,
        boolean processed,
        String originNoticeId
) {
}
