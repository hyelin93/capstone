package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Notice {

    // 기존 API용
    private Long id;
    private String title;
    private String date;
    private String link;
    private String category;

    // 크롤링/알림용
    private String content;
    private String keyword;
    private LocalDateTime crawledAt;
    private boolean processed;
    private String originNoticeId;
}