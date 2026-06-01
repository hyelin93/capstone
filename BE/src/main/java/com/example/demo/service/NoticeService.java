package com.example.demo.service;

import com.example.demo.crawler.NoticeCrawler;
import com.example.demo.dto.Notice;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoticeService {
    private final NoticeCrawler noticeCrawler;

    public NoticeService(NoticeCrawler noticeCrawler) {
        this.noticeCrawler = noticeCrawler;
    }

    public List<Notice> getLatestNotices() {
        return noticeCrawler.crawlAcademicNotices();
    }
}
