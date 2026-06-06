package com.example.demo.controller;

import com.example.demo.dto.Notice;
import com.example.demo.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {
    private final NoticeService noticeService;

    @GetMapping
    // 테스트용 API로 최신 공지 목록을 크롤링하고 저장된 공지 목록을 HTTP 응답으로 반환합니다.
    public List<Notice> getNotices() {
        return noticeService.getLatestNotices();
    }
}
