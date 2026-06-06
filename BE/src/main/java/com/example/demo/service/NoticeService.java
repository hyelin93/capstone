package com.example.demo.service;

import com.example.demo.crawler.NoticeCrawler;
import com.example.demo.dto.Notice;
import com.example.demo.entity.NoticeEntity;
import com.example.demo.repository.NoticeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NoticeService {

    private static final Logger log = LoggerFactory.getLogger(NoticeService.class);

    private final NoticeCrawler noticeCrawler;
    private final NoticeRepository noticeRepository;

    public NoticeService(NoticeCrawler noticeCrawler, NoticeRepository noticeRepository) {
        this.noticeCrawler = noticeCrawler;
        this.noticeRepository = noticeRepository;
    }

    @Transactional
    public List<Notice> getLatestNotices() {
        crawlAndSaveLatestNotices();

        return noticeRepository.findAllByOrderByCrawledAtDescNoticeIdDesc()
                .stream()
                .map(NoticeEntity::toDto)
                .toList();
    }

    @Transactional
    public void crawlAndSaveLatestNotices() {
        List<Notice> crawledNotices = noticeCrawler.crawlNoticeBoards();
        saveNewNotices(crawledNotices);
    }

    @Transactional(readOnly = true)
    public List<Notice> getNotices(String category, String keyword, int page, int size) {
        return noticeRepository.findAllByOrderByCrawledAtDescNoticeIdDesc()
                .stream()
                .map(NoticeEntity::toDto)
                .filter(notice -> category == null
                        || category.equals("전체")
                        || notice.getCategory().equals(category))
                .filter(notice -> keyword == null
                        || notice.getTitle().contains(keyword))
                .skip((long) page * size)
                .limit(size)
                .toList();
    }

    @Transactional(readOnly = true)
    public Notice getNoticeDetail(Long id) {
        return noticeRepository.findById(id)
                .map(NoticeEntity::toDto)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));
    }

    private void saveNewNotices(List<Notice> crawledNotices) {
        long startedAt = System.nanoTime();

        log.info("공지 DB 저장 시작: crawledCount={}", crawledNotices.size());

        try {
            Set<String> crawledLinks = crawledNotices.stream()
                    .map(Notice::getLink)
                    .filter(link -> link != null && !link.isBlank())
                    .collect(Collectors.toSet());

            int validLinkCount = crawledLinks.size();
            int invalidLinkCount = crawledNotices.size() - validLinkCount;

            if (crawledLinks.isEmpty()) {
                log.info("저장할 공지가 없습니다.");
                return;
            }

            Set<String> existingLinks = noticeRepository.findExistingUrls(crawledLinks);

            Map<String, NoticeEntity> newNoticesByLink = new LinkedHashMap<>();

            for (Notice notice : crawledNotices) {
                if (notice.getLink() == null
                        || notice.getLink().isBlank()
                        || existingLinks.contains(notice.getLink())) {
                    continue;
                }

                newNoticesByLink.putIfAbsent(
                        notice.getLink(),
                        NoticeEntity.from(notice)
                );
            }

            if (!newNoticesByLink.isEmpty()) {
                noticeRepository.saveAll(new ArrayList<>(newNoticesByLink.values()));
            }

            log.info(
                    "공지 DB 저장 완료: crawledCount={}, validLinkCount={}, invalidLinkCount={}, alreadySavedCount={}, savedCount={}, elapsedMs={}",
                    crawledNotices.size(),
                    validLinkCount,
                    invalidLinkCount,
                    existingLinks.size(),
                    newNoticesByLink.size(),
                    elapsedMillis(startedAt)
            );

        } catch (RuntimeException e) {
            log.error("공지 DB 저장 실패", e);
            throw e;
        }
    }

    private long elapsedMillis(long startedAt) {
        return Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
    }
}