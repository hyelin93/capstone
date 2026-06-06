package com.example.demo.controller;

import com.example.demo.dto.Notice;
import com.example.demo.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 공지 목록 조회
     * 카테고리, 키워드 검색, 페이징 지원
     */
    @GetMapping
    public List<Notice> getNotices(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return noticeService.getNotices(category, keyword, page, size);
    }

    /**
     * 공지 상세 조회
     */
    @GetMapping("/{id}")
    public Notice getNoticeDetail(@PathVariable Long id) {
        return noticeService.getNoticeDetail(id);
    }

    /**
     * 테스트용 크롤링 API
     * 최신 공지를 크롤링하여 반환
     */
    @GetMapping("/latest")
    public List<Notice> getLatestNotices() {
        return noticeService.getLatestNotices();
    }
}