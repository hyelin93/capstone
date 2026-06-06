package com.example.demo.service;

import com.example.demo.crawler.NoticeCrawler;
import com.example.demo.dto.Notice;
import com.example.demo.entity.NoticeEntity;
import com.example.demo.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeCrawler noticeCrawler;
    private final NoticeRepository noticeRepository;

    // 최신 공지 게시판 목록을 크롤링한 뒤 신규 공지만 DB에 저장하고 저장된 목록을 반환합니다.
    @Transactional
    public List<Notice> getLatestNotices() {
        crawlAndSaveLatestNotices();

        return noticeRepository.findAllByOrderByCrawledAtDescNoticeIdDesc()
                .stream()
                .map(NoticeEntity::toDto)
                .toList();
    }

    // 최신 공지 게시판 목록을 크롤링한 뒤 신규 공지만 DB에 저장합니다.
    @Transactional
    public void crawlAndSaveLatestNotices() {
        List<Notice> crawledNotices = noticeCrawler.crawlNoticeBoards();
        saveNewNotices(crawledNotices);
    }

    // 크롤링된 공지 중 아직 저장되지 않은 신규 공지만 DB에 저장합니다.
    private void saveNewNotices(List<Notice> crawledNotices) {
        long startedAt = System.nanoTime();
        log.info("공지 DB 저장 시작: crawledCount={}", crawledNotices.size());
        try {
            Set<String> crawledUrls = crawledNotices.stream()
                    .map(Notice::url)
                    .filter(url -> url != null && !url.isBlank())
                    .collect(Collectors.toSet());
            int validUrlCount = (int) crawledNotices.stream()
                    .map(Notice::url)
                    .filter(url -> url != null && !url.isBlank())
                    .count();
            int invalidUrlCount = crawledNotices.size() - validUrlCount;
            int duplicateInCrawlCount = validUrlCount - crawledUrls.size();
            if (crawledUrls.isEmpty()) {
                log.info(
                        "공지 DB 저장 완료: crawledCount={}, validUrlCount=0, invalidUrlCount={}, duplicateInCrawlCount=0, alreadySavedCount=0, savedCount=0, elapsedMs={}",
                        crawledNotices.size(),
                        invalidUrlCount,
                        elapsedMillis(startedAt)
                );
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

            log.info(
                    "공지 DB 저장 완료: crawledCount={}, validUrlCount={}, invalidUrlCount={}, duplicateInCrawlCount={}, alreadySavedCount={}, savedCount={}, elapsedMs={}",
                    crawledNotices.size(),
                    validUrlCount,
                    invalidUrlCount,
                    duplicateInCrawlCount,
                    existingUrls.size(),
                    newNoticesByUrl.size(),
                    elapsedMillis(startedAt)
            );
        } catch (RuntimeException e) {
            log.error("공지 DB 저장 실패: crawledCount={}, elapsedMs={}", crawledNotices.size(), elapsedMillis(startedAt), e);
            throw e;
        }
    }

    // 시작 시각부터 현재까지 걸린 시간을 밀리초 단위로 계산합니다.
    private long elapsedMillis(long startedAt) {
        return Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
    }
}
