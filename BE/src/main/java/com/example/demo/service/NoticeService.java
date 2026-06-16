package com.example.demo.service;

import com.example.demo.adapter.NoticeAdapter;
import com.example.demo.crawler.NoticeCrawler;
import com.example.demo.dto.Notice;
import com.example.demo.repository.NoticeRepository;
import com.example.demo.entity.NoticeEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
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
    private static final String TEST_NOTICE_TITLE = "2026학년도 2학기 장학금 신청 안내";
    private static final String TEST_NOTICE_CONTENT = """
            2026학년도 2학기 재학생 장학금 신청 일정을 아래와 같이 안내합니다.

            신청 대상은 2026학년도 2학기 등록 예정인 재학생이며, 직전 학기 성적과 소득 구간, 교내 장학 기준을 종합하여 선발합니다. 장학금 신청을 희망하는 학생은 기간 내 신청서와 증빙서류를 제출해 주시기 바랍니다.

            신청 기간: 2026년 6월 20일 10:00 ~ 2026년 7월 5일 17:00
            제출 서류: 장학금 신청서, 성적증명서, 소득 관련 증빙서류
            제출 방법: 학생지원팀 이메일 제출 또는 방문 제출
            문의: 학생지원팀 02-0000-0000

            기간 내 서류를 제출하지 않거나 필수 서류가 누락된 경우 장학금 심사 대상에서 제외될 수 있습니다. 세부 선발 기준과 지급 일정은 심사 완료 후 개별 안내됩니다.
            """;

    private final NoticeCrawler noticeCrawler;
    private final NoticeRepository noticeRepository;
    private final NoticeAdapter noticeAdapter;
    private final NotificationService notificationService;

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

    @Transactional
    public Notice getNoticeDetail(Integer id) {
        NoticeEntity entity = noticeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "공지사항을 찾을 수 없습니다."));
        fillContentIfMissing(entity);
        return noticeAdapter.toDto(entity);
    }

    @Transactional
    public Notice createTestNotice() {
        String originNoticeId = "test-notice-" + System.currentTimeMillis();
        Notice testNotice = new Notice(
                null,
                TEST_NOTICE_TITLE,
                "https://www.syu.ac.kr/test/notices/" + originNoticeId + "/",
                TEST_NOTICE_CONTENT,
                "학생지원팀",
                "학사",
                LocalDateTime.now(),
                false,
                originNoticeId
        );

        NoticeEntity savedEntity = noticeRepository.save(noticeAdapter.toEntity(testNotice));
        Notice savedNotice = noticeAdapter.toDto(savedEntity);
        notificationService.sendNoticeIfKeywordMatched(savedNotice);
        return savedNotice;
    }

    @Transactional
    public void crawlAndSaveLatestNotices() {
        List<Notice> crawledNotices = noticeCrawler.crawlNoticeBoards();
        saveNewNotices(crawledNotices);
    }

    private List<Notice> findSavedNotices() {
        return noticeRepository.findAllByOrderByCrawledAtDescNoticeIdDesc()
                .stream()
                .map(noticeAdapter::toDto)
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
            int updatedExistingContentCount = updateMissingContents(crawledNotices, existingUrls);
            Map<String, NoticeEntity> newNoticesByUrl = new LinkedHashMap<>();
            for (Notice notice : crawledNotices) {
                if (notice.url() == null || notice.url().isBlank() || existingUrls.contains(notice.url())) {
                    continue;
                }

                newNoticesByUrl.putIfAbsent(notice.url(), noticeAdapter.toEntity(notice));
            }

            if (!newNoticesByUrl.isEmpty()) {
                noticeRepository.saveAll(new ArrayList<>(newNoticesByUrl.values()));

                for (Notice notice : crawledNotices) {
                    if (newNoticesByUrl.containsKey(notice.url())) {
                        notificationService.sendNoticeIfKeywordMatched(notice);
                    }
                }
            }

            log.info(
                    "공지 DB 저장 완료: crawledCount={}, validUrlCount={}, invalidUrlCount={}, duplicateInCrawlCount={}, alreadySavedCount={}, updatedExistingContentCount={}, savedCount={}, elapsedMs={}",
                    crawledNotices.size(),
                    validUrlCount,
                    invalidUrlCount,
                    duplicateInCrawlCount,
                    existingUrls.size(),
                    updatedExistingContentCount,
                    newNoticesByUrl.size(),
                    elapsedMillis(startedAt)
            );
        } catch (RuntimeException e) {
            log.error("공지 DB 저장 실패: crawledCount={}, elapsedMs={}", crawledNotices.size(), elapsedMillis(startedAt), e);
            throw e;
        }
    }

    private void fillContentIfMissing(NoticeEntity entity) {
        if (hasText(entity.getContent())) {
            return;
        }

        String content = noticeCrawler.crawlNoticeContent(entity.getUrl());
        if (hasText(content)) {
            entity.updateContent(content);
        }
    }

    private int updateMissingContents(List<Notice> crawledNotices, Set<String> existingUrls) {
        Map<String, String> contentsByUrl = crawledNotices.stream()
                .filter(notice -> notice.url() != null && existingUrls.contains(notice.url()))
                .filter(notice -> hasText(notice.content()))
                .collect(Collectors.toMap(
                        Notice::url,
                        Notice::content,
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
        if (contentsByUrl.isEmpty()) {
            return 0;
        }

        int updatedCount = 0;
        for (NoticeEntity entity : noticeRepository.findByUrlIn(contentsByUrl.keySet())) {
            if (!hasText(entity.getContent())) {
                entity.updateContent(contentsByUrl.get(entity.getUrl()));
                updatedCount++;
            }
        }

        return updatedCount;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private long elapsedMillis(long startedAt) {
        return Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
    }
}
