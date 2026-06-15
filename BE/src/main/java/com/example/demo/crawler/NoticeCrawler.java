package com.example.demo.crawler;

import com.example.demo.dto.Notice;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Component
public class NoticeCrawler {
    private static final int TIMEOUT_MILLIS = (int) Duration.ofSeconds(5).toMillis();
    private static final int MAX_CRAWL_ATTEMPTS = 3;
    private static final String NOTICE_LIST_LINK_SELECTOR =
            "td.step2 a.itembx[href], a.itembx[href]:has(span.tit), table a[href]:has(span.tit)";
    private static final List<String> NOTICE_CONTENT_SELECTORS = List.of(
            ".single_cont",
            ".single_contbx",
            "article .entry-content",
            ".entry-content"
    );
    private static final List<CrawlTarget> NOTICE_TARGETS = List.of(
            new CrawlTarget("학사공지", "https://www.syu.ac.kr/academic/academic-notice/"),
            new CrawlTarget("행사공지", "https://www.syu.ac.kr/university-square/notice/event/"),
            new CrawlTarget("생활공지", "https://www.syu.ac.kr/university-square/notice/campus-notice/"),
            new CrawlTarget("취업·창업공지", "https://www.syu.ac.kr/university-square/notice/employment-founding-announcement/"),
            new CrawlTarget("외부공지", "https://www.syu.ac.kr/university-square/notice/external-notice/"),
            new CrawlTarget("추천채용", "https://www.syu.ac.kr/university-square/notice/job-offer/"),
            new CrawlTarget("채용공고", "https://www.syu.ac.kr/university-square/notice/recruitment/")
    );

    private final List<CrawlTarget> noticeTargets;
    private final NoticeDocumentFetcher documentFetcher;

    public NoticeCrawler() {
        this(NOTICE_TARGETS, NoticeCrawler::fetchDocument);
    }

    NoticeCrawler(List<CrawlTarget> noticeTargets, NoticeDocumentFetcher documentFetcher) {
        this.noticeTargets = noticeTargets;
        this.documentFetcher = documentFetcher;
    }

    record CrawlTarget(String category, String url) {
    }

    @FunctionalInterface
    interface NoticeDocumentFetcher {
        Document fetch(String listUrl) throws IOException;
    }

    public List<Notice> crawlNoticeBoards() {
        long startedAt = System.nanoTime();
        int crawledCount = 0;
        List<String> failedCategories = new ArrayList<>();
        Map<String, Notice> noticesByUrl = new LinkedHashMap<>();
        log.info("공지 게시판 전체 크롤링 시작: boardCount={}", noticeTargets.size());

        for (CrawlTarget target : noticeTargets) {
            try {
                List<Notice> notices = crawl(target.url(), target.category());
                crawledCount += notices.size();
                for (Notice notice : notices) {
                    noticesByUrl.putIfAbsent(notice.url(), notice);
                }
            } catch (IllegalStateException e) {
                failedCategories.add(target.category());
                log.warn(
                        "공지 게시판 크롤링 실패로 제외: category={}, url={}, reason={}",
                        target.category(),
                        target.url(),
                        e.getMessage()
                );
            }
        }

        List<Notice> uniqueNotices = new ArrayList<>(noticesByUrl.values());
        if (failedCategories.isEmpty()) {
            log.info(
                    "공지 게시판 전체 크롤링 완료: boardCount={}, successBoardCount={}, failedBoardCount=0, crawledCount={}, uniqueCount={}, duplicateCount={}, elapsedMs={}",
                    noticeTargets.size(),
                    noticeTargets.size(),
                    crawledCount,
                    uniqueNotices.size(),
                    crawledCount - uniqueNotices.size(),
                    elapsedMillis(startedAt)
            );
        } else {
            log.warn(
                    "공지 게시판 부분 크롤링 완료: boardCount={}, successBoardCount={}, failedBoardCount={}, failedCategories={}, crawledCount={}, uniqueCount={}, duplicateCount={}, elapsedMs={}",
                    noticeTargets.size(),
                    noticeTargets.size() - failedCategories.size(),
                    failedCategories.size(),
                    failedCategories,
                    crawledCount,
                    uniqueNotices.size(),
                    crawledCount - uniqueNotices.size(),
                    elapsedMillis(startedAt)
            );
        }
        return uniqueNotices;
    }

    private List<Notice> crawl(String listUrl, String category) {
        long startedAt = System.nanoTime();
        IOException lastException = null;
        log.info("공지 크롤링 시작: category={}, url={}, maxAttempts={}", category, listUrl, MAX_CRAWL_ATTEMPTS);

        for (int attempt = 1; attempt <= MAX_CRAWL_ATTEMPTS; attempt++) {
            long attemptStartedAt = System.nanoTime();
            try {
                Document document = documentFetcher.fetch(listUrl);

                List<Notice> notices = parseNoticeList(document, category)
                        .stream()
                        .map(this::withFetchedContent)
                        .toList();
                log.info(
                        "공지 크롤링 완료: category={}, url={}, attempt={}, count={}, attemptElapsedMs={}, elapsedMs={}",
                        category,
                        listUrl,
                        attempt,
                        notices.size(),
                        elapsedMillis(attemptStartedAt),
                        elapsedMillis(startedAt)
                );
                return notices;
            } catch (IOException e) {
                lastException = e;
                if (attempt < MAX_CRAWL_ATTEMPTS) {
                    log.warn(
                            "공지 크롤링 재시도 예정: category={}, url={}, attempt={}, maxAttempts={}, attemptElapsedMs={}",
                            category,
                            listUrl,
                            attempt,
                            MAX_CRAWL_ATTEMPTS,
                            elapsedMillis(attemptStartedAt),
                            e
                    );
                    continue;
                }

                log.error(
                        "공지 크롤링 최종 실패: category={}, url={}, attempts={}, elapsedMs={}",
                        category,
                        listUrl,
                        MAX_CRAWL_ATTEMPTS,
                        elapsedMillis(startedAt),
                        e
                );
            }
        }

        throw new IllegalStateException("공지 목록을 가져오지 못했습니다: " + listUrl, lastException);
    }

    private static Document fetchDocument(String listUrl) throws IOException {
        return Jsoup.connect(listUrl)
                .userAgent("Mozilla/5.0 (compatible; SYU-Capstone-NoticeCrawler/1.0)")
                .timeout(TIMEOUT_MILLIS)
                .get();
    }

    List<Notice> parseNoticeList(Document document, String category) {
        Map<String, Notice> noticesByUrl = new LinkedHashMap<>();
        for (Element link : document.select(NOTICE_LIST_LINK_SELECTOR)) {
            Notice notice = toNotice(link, category);
            if (!notice.title().isBlank() && !notice.url().isBlank()) {
                noticesByUrl.putIfAbsent(notice.url(), notice);
            }
        }

        return new ArrayList<>(noticesByUrl.values());
    }

    public String crawlNoticeContent(String noticeUrl) {
        return fetchNoticeContent(noticeUrl, noticeUrl);
    }

    private Notice withFetchedContent(Notice notice) {
        String content = fetchNoticeContent(notice.url(), notice.title());
        return new Notice(
                notice.noticeId(),
                notice.title(),
                notice.url(),
                content,
                notice.department(),
                notice.keyword(),
                notice.crawledAt(),
                notice.processed(),
                notice.originNoticeId()
        );
    }

    private String fetchNoticeContent(String noticeUrl, String title) {
        try {
            Document detailDocument = documentFetcher.fetch(noticeUrl);
            String content = extractNoticeContent(detailDocument);
            if (content.isBlank()) {
                log.warn("공지 본문이 비어 있습니다: title={}, url={}", title, noticeUrl);
            }

            return content;
        } catch (IOException e) {
            log.warn("공지 본문 크롤링 실패로 빈 본문을 사용합니다: title={}, url={}", title, noticeUrl, e);
            return "";
        }
    }

    String extractNoticeContent(Document document) {
        Element content = findNoticeContent(document);
        if (content == null) {
            return "";
        }

        content.select("script, style, noscript, .md_single_headbx, .md_single_share").remove();
        return text(content);
    }

    private Element findNoticeContent(Document document) {
        for (String selector : NOTICE_CONTENT_SELECTORS) {
            Element content = document.selectFirst(selector);
            if (content != null) {
                return content;
            }
        }

        return null;
    }

    private Notice toNotice(Element link, String category) {
        Element row = link.closest("tr");
        String rawUrl = link.absUrl("href");
        if (rawUrl.isBlank()) {
            rawUrl = link.attr("href");
        }
        String url = canonicalize(rawUrl);

        String inlineCategory = text(link.selectFirst("span.md_cate"));
        String title = cleanTitle(text(link.selectFirst("span.tit")), inlineCategory, link.text());
        String department = row == null ? "" : text(row.selectFirst("td.step3"));

        return new Notice(
                null,
                title,
                url,
                "",
                department,
                toKeyword(category),
                null,
                false,
                extractId(url)
        );
    }

    private String cleanTitle(String title, String category, String fallback) {
        String normalizedTitle = normalize(title);
        if (normalizedTitle.isBlank()) {
            normalizedTitle = normalize(fallback);
            if (!category.isBlank()) {
                normalizedTitle = normalizedTitle.replaceFirst("^" + Pattern.quote(category) + "\\s+", "");
            }
        }

        return normalizedTitle.replaceFirst("\\s+NEW$", "");
    }

    private String canonicalize(String url) {
        try {
            URI uri = URI.create(url);
            return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, null).toString();
        } catch (IllegalArgumentException | URISyntaxException e) {
            return url;
        }
    }

    private String extractId(String url) {
        try {
            String path = URI.create(url).getPath();
            String[] segments = path.split("/");
            for (int i = segments.length - 1; i >= 0; i--) {
                if (!segments[i].isBlank()) {
                    return segments[i];
                }
            }
        } catch (IllegalArgumentException ignored) {
        }

        return Integer.toHexString(url.hashCode());
    }

    private String toKeyword(String category) {
        return switch (category) {
            case "학사공지", "학사" -> "학사";
            case "행사공지", "행사" -> "행사";
            case "생활공지", "생활" -> "생활";
            case "취업·창업공지", "취업창업공지", "취창업" -> "취창업";
            case "외부공지", "외부" -> "외부";
            case "추천채용" -> "추천채용";
            case "채용공고" -> "채용공고";
            default -> category;
        };
    }

    private String text(Element element) {
        if (element == null) {
            return "";
        }

        return normalize(element.text());
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.replaceAll("\\s+", " ").trim();
    }

    private long elapsedMillis(long startedAt) {
        return Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
    }
}
