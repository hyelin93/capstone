package com.example.demo.crawler;

import com.example.demo.dto.Notice;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Component
public class NoticeCrawler {
    private static final Logger log = LoggerFactory.getLogger(NoticeCrawler.class);
    private static final int TIMEOUT_MILLIS = (int) Duration.ofSeconds(5).toMillis();
    private static final int MAX_CRAWL_ATTEMPTS = 3;
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

    // 기본 크롤링 대상과 Jsoup 요청 함수를 사용해 크롤러를 생성합니다.
    public NoticeCrawler() {
        this(NOTICE_TARGETS, NoticeCrawler::fetchDocument);
    }

    // 테스트에서 크롤링 대상과 문서 요청 함수를 주입할 수 있게 크롤러를 생성합니다.
    NoticeCrawler(List<CrawlTarget> noticeTargets, NoticeDocumentFetcher documentFetcher) {
        this.noticeTargets = noticeTargets;
        this.documentFetcher = documentFetcher;
    }

    record CrawlTarget(String category, String url) {
    }

    @FunctionalInterface
    interface NoticeDocumentFetcher {
        // 전달받은 목록 URL의 HTML 문서를 가져옵니다.
        Document fetch(String listUrl) throws IOException;
    }

    // 문서에 정의된 공지 게시판들의 첫 페이지에서 공지 데이터를 크롤링합니다.
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

    // 전달받은 목록 URL을 요청하고 공지 항목 목록으로 파싱합니다.
    private List<Notice> crawl(String listUrl, String category) {
        long startedAt = System.nanoTime();
        IOException lastException = null;
        log.info("공지 크롤링 시작: category={}, url={}, maxAttempts={}", category, listUrl, MAX_CRAWL_ATTEMPTS);

        for (int attempt = 1; attempt <= MAX_CRAWL_ATTEMPTS; attempt++) {
            long attemptStartedAt = System.nanoTime();
            try {
                Document document = documentFetcher.fetch(listUrl);

                List<Notice> notices = parseNoticeList(document, category);
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

    // Jsoup으로 전달받은 목록 URL의 HTML 문서를 요청합니다.
    private static Document fetchDocument(String listUrl) throws IOException {
        return Jsoup.connect(listUrl)
                .userAgent("Mozilla/5.0 (compatible; SYU-Capstone-NoticeCrawler/1.0)")
                .timeout(TIMEOUT_MILLIS)
                .get();
    }

    // HTML 문서에서 공지 링크 행을 찾아 중복 없는 공지 목록으로 변환합니다.
    List<Notice> parseNoticeList(Document document, String category) {
        Map<String, Notice> noticesByUrl = new LinkedHashMap<>();
        for (Element link : document.select("td.step2 a.itembx[href], a.itembx[href]:has(span.tit), table a[href]:has(span.tit)")) {
            Notice notice = toNotice(link, category);
            if (!notice.title().isBlank() && !notice.url().isBlank()) {
                noticesByUrl.putIfAbsent(notice.url(), notice);
            }
        }

        return new ArrayList<>(noticesByUrl.values());
    }

    // 공지 링크 요소와 행 정보를 Notice DTO로 변환합니다.
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

    // 제목 문자열에서 불필요한 카테고리와 신규 표시를 제거합니다.
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

    // URL에서 쿼리 문자열과 fragment를 제거한 표준 URL을 만듭니다.
    private String canonicalize(String url) {
        try {
            URI uri = URI.create(url);
            return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, null).toString();
        } catch (IllegalArgumentException | URISyntaxException e) {
            return url;
        }
    }

    // 공지 URL의 마지막 경로 조각을 식별자로 추출합니다.
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

    // 크롤링한 게시판명을 저장용 키워드 값으로 변환합니다.
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

    // 요소의 텍스트를 안전하게 읽고 공백을 정리합니다.
    private String text(Element element) {
        if (element == null) {
            return "";
        }

        return normalize(element.text());
    }

    // 문자열의 연속 공백을 하나로 줄이고 앞뒤 공백을 제거합니다.
    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.replaceAll("\\s+", " ").trim();
    }

    // 시작 시각부터 현재까지 걸린 시간을 밀리초 단위로 계산합니다.
    private long elapsedMillis(long startedAt) {
        return Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
    }
}