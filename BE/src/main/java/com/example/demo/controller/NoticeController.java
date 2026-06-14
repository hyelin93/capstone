package com.example.demo.controller;

import com.example.demo.dto.Notice;
import com.example.demo.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {
    private final NoticeService noticeService;

    @GetMapping
    public List<Notice> getNotices(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return noticeService.getNotices(category, keyword, page, size);
    }

    @GetMapping("/latest")
    public List<Notice> getLatestNotices() {
        return noticeService.getLatestNotices();
    }

    @GetMapping("/{id}")
    public Notice getNoticeDetail(@PathVariable Integer id) {
        return noticeService.getNoticeDetail(id);
    }
}
