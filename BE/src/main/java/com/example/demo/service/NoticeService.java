package com.example.demo.service;

import com.example.demo.crawler.NoticeCrawler;
import com.example.demo.dto.Notice;
import com.example.demo.entity.NoticeEntity;
import com.example.demo.repository.NoticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NoticeService {
    private final NoticeCrawler noticeCrawler;
    private final NoticeRepository noticeRepository;

    // 공지 서비스에서 사용할 크롤러 의존성을 주입받습니다.
    public NoticeService(NoticeCrawler noticeCrawler, NoticeRepository noticeRepository) {
        this.noticeCrawler = noticeCrawler;
        this.noticeRepository = noticeRepository;
    }

    // 최신 공지 게시판 목록을 크롤링한 뒤 신규 공지만 DB에 저장하고 저장된 목록을 반환합니다.
    @Transactional
    public List<Notice> getLatestNotices() {
        List<Notice> crawledNotices = noticeCrawler.crawlNoticeBoards();
        saveNewNotices(crawledNotices);

        return noticeRepository.findAllByOrderByPublishedDateDescIdDesc()
                .stream()
                .map(NoticeEntity::toDto)
                .toList();
    }

    private void saveNewNotices(List<Notice> crawledNotices) {
        Set<String> crawledUrls = crawledNotices.stream()
                .map(Notice::url)
                .filter(url -> url != null && !url.isBlank())
                .collect(Collectors.toSet());
        if (crawledUrls.isEmpty()) {
            return;
        }

        Set<String> existingUrls = noticeRepository.findExistingUrls(crawledUrls);
        Map<String, NoticeEntity> newNoticesByUrl = new LinkedHashMap<>();
        for (Notice notice : crawledNotices) {
            if (notice.url() == null || notice.url().isBlank() || existingUrls.contains(notice.url())) {
                continue;
            }

            newNoticesByUrl.putIfAbsent(notice.url(), NoticeEntity.from(notice));
        }

        if (!newNoticesByUrl.isEmpty()) {
            noticeRepository.saveAll(new ArrayList<>(newNoticesByUrl.values()));
        }
    }
}
