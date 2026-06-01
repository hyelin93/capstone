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
    private static final String ACADEMIC_NOTICE_URL = "https://www.syu.ac.kr/academic/academic-notice/";
    private static final String ACADEMIC_NOTICE_SOURCE = "학사공지";
    private static final int TIMEOUT_MILLIS = (int) Duration.ofSeconds(5).toMillis();
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{4})[.\\-/](\\d{1,2})[.\\-/](\\d{1,2})");

    public List<Notice> crawlAcademicNotices() {
        return crawl(ACADEMIC_NOTICE_URL, ACADEMIC_NOTICE_SOURCE);
    }

    public List<Notice> crawl(String listUrl, String source) {
        try {
            Document document = Jsoup.connect(listUrl)
                    .userAgent("Mozilla/5.0 (compatible; SYU-Capstone-NoticeCrawler/1.0)")
                    .timeout(TIMEOUT_MILLIS)
                    .get();

            return parseNoticeList(document, source);
        } catch (IOException e) {
            throw new IllegalStateException("공지 목록을 가져오지 못했습니다: " + listUrl, e);
        }
    }

    List<Notice> parseNoticeList(Document document, String source) {
        Map<String, Notice> noticesByUrl = new LinkedHashMap<>();
        for (Element link : document.select("td.step2 a.itembx[href], a.itembx[href]:has(span.tit), table a[href]:has(span.tit)")) {
            Notice notice = toNotice(link, source);
            if (!notice.title().isBlank() && !notice.url().isBlank()) {
                noticesByUrl.putIfAbsent(notice.url(), notice);
            }
        }

        return new ArrayList<>(noticesByUrl.values());
    }

    private Notice toNotice(Element link, String source) {
        Element row = link.closest("tr");
        String rawUrl = link.absUrl("href");
        if (rawUrl.isBlank()) {
            rawUrl = link.attr("href");
        }
        String url = canonicalize(rawUrl);

        String category = text(link.selectFirst("span.md_cate"));
        String title = cleanTitle(text(link.selectFirst("span.tit")), category, link.text());
        String author = row == null ? "" : text(row.selectFirst("td.step3"));
        LocalDate publishedDate = row == null ? null : parseDate(row.text());

        return new Notice(
                extractId(url),
                title,
                category,
                author,
                publishedDate,
                url,
                source
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
}
