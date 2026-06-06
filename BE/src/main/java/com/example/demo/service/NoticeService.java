package com.example.demo.service;

import com.example.demo.crawler.NoticeCrawler;
import com.example.demo.dto.Notice;
import com.example.demo.entity.NoticeEntity;
import com.example.demo.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeCrawler noticeCrawler;
    private final NoticeRepository noticeRepository;

    @Transactional
    public List<Notice> getLatestNotices() {
        crawlAndSaveLatestNotices();
        return findSavedNotices();
    }

    @Transactional
    public List<Notice> getNotices(String category, String keyword, Integer page, Integer size) {
        crawlAndSaveLatestNotices();

        Stream<Notice> notices = findSavedNotices().stream()
                .filter(notice -> matchesCategory(notice, category))
                .filter(notice -> matchesKeyword(notice, keyword));

        if (page != null && size != null && size > 0) {
            notices = notices
                    .skip((long) Math.max(page, 0) * size)
                    .limit(size);
        }

        return notices.toList();
    }

    @Transactional(readOnly = true)
    public Notice getNoticeDetail(Integer id) {
        return noticeRepository.findById(id)
                .map(NoticeEntity::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "공지사항을 찾을 수 없습니다."));
    }

    @Transactional
    public void crawlAndSaveLatestNotices() {
        List<Notice> crawledNotices = noticeCrawler.crawlNoticeBoards();
        saveNewNotices(crawledNotices);
    }

    private List<Notice> findSavedNotices() {
        return noticeRepository.findAllByOrderByCrawledAtDescNoticeIdDesc()
                .stream()
                .map(NoticeEntity::toDto)
                .toList();
    }

    private boolean matchesCategory(Notice notice, String category) {
        if (category == null || category.isBlank() || "전체".equals(category)) {
            return true;
        }

        return category.equals(notice.keyword());
    }

    private boolean matchesKeyword(Notice notice, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        String normalizedKeyword = keyword.trim();
        return contains(notice.title(), normalizedKeyword)
                || contains(notice.content(), normalizedKeyword)
                || contains(notice.department(), normalizedKeyword)
                || contains(notice.keyword(), normalizedKeyword);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.contains(keyword);
    }

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

    private long elapsedMillis(long startedAt) {
        return Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
    }
}
