package com.example.demo.crawler;

import com.example.demo.dto.Notice;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class NoticeCrawler {
    private static final int TIMEOUT_MILLIS = (int) Duration.ofSeconds(5).toMillis();
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{4})[.\\-/](\\d{1,2})[.\\-/](\\d{1,2})");
    private static final List<CrawlTarget> NOTICE_TARGETS = List.of(
            new CrawlTarget("학사공지", "https://www.syu.ac.kr/academic/academic-notice/"),
            new CrawlTarget("행사공지", "https://www.syu.ac.kr/university-square/notice/event/"),
            new CrawlTarget("생활공지", "https://www.syu.ac.kr/university-square/notice/campus-notice/"),
            new CrawlTarget("취업·창업공지", "https://www.syu.ac.kr/university-square/notice/employment-founding-announcement/"),
            new CrawlTarget("외부공지", "https://www.syu.ac.kr/university-square/notice/external-notice/"),
            new CrawlTarget("추천채용", "https://www.syu.ac.kr/university-square/notice/job-offer/"),
            new CrawlTarget("채용공고", "https://www.syu.ac.kr/university-square/notice/recruitment/")
    );

    private record CrawlTarget(String category, String url) {
    }

    // 문서에 정의된 공지 게시판들의 첫 페이지에서 공지 데이터를 크롤링합니다.
    public List<Notice> crawlNoticeBoards() {
        Map<String, Notice> noticesByUrl = new LinkedHashMap<>();
        for (CrawlTarget target : NOTICE_TARGETS) {
            for (Notice notice : crawl(target.url(), target.category())) {
                noticesByUrl.putIfAbsent(notice.url(), notice);
            }
        }

        return new ArrayList<>(noticesByUrl.values());
    }

    // 전달받은 목록 URL을 요청하고 공지 항목 목록으로 파싱합니다.
    public List<Notice> crawl(String listUrl, String category) {
        try {
            Document document = Jsoup.connect(listUrl)
                    .userAgent("Mozilla/5.0 (compatible; SYU-Capstone-NoticeCrawler/1.0)")
                    .timeout(TIMEOUT_MILLIS)
                    .get();

            return parseNoticeList(document, category);
        } catch (IOException e) {
            throw new IllegalStateException("공지 목록을 가져오지 못했습니다: " + listUrl, e);
        }
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
        String author = row == null ? "" : text(row.selectFirst("td.step3"));
        LocalDate publishedDate = row == null ? null : parseDate(row.text());

        return new Notice(
                extractId(url),
                title,
                category,
                author,
                publishedDate,
                url,
                category
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

    // 문자열에 포함된 날짜를 LocalDate로 변환합니다.
    private LocalDate parseDate(String value) {
        Matcher matcher = DATE_PATTERN.matcher(value);
        if (!matcher.find()) {
            return null;
        }

        return LocalDate.of(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3))
        );
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
}
