package com.example.demo.dto;

import java.time.LocalDateTime;

public class Notice {
    private Integer noticeId;
    private String title;
    private String url;
    private String content;
    private String department;
    private String keyword;
    private LocalDateTime crawledAt;
    private boolean processed;
    private String originNoticeId;

    public Notice() {
    }

    public Notice(
            Integer noticeId,
            String title,
            String url,
            String content,
            String department,
            String keyword,
            LocalDateTime crawledAt,
            boolean processed,
            String originNoticeId
    ) {
        this.noticeId = noticeId;
        this.title = title;
        this.url = url;
        this.content = content;
        this.department = department;
        this.keyword = keyword;
        this.crawledAt = crawledAt;
        this.processed = processed;
        this.originNoticeId = originNoticeId;
    }

    public Integer noticeId() {
        return noticeId;
    }

    public String title() {
        return title;
    }

    public String url() {
        return url;
    }

    public String content() {
        return content;
    }

    public String department() {
        return department;
    }

    public String keyword() {
        return keyword;
    }

    public LocalDateTime crawledAt() {
        return crawledAt;
    }

    public boolean processed() {
        return processed;
    }

    public String originNoticeId() {
        return originNoticeId;
    }

    public Integer getNoticeId() {
        return noticeId;
    }

    public Long getId() {
        return noticeId == null ? null : noticeId.longValue();
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getLink() {
        return url;
    }

    public String getContent() {
        return content;
    }

    public String getDepartment() {
        return department;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getCategory() {
        return keyword;
    }

    public LocalDateTime getCrawledAt() {
        return crawledAt;
    }

    public String getDate() {
        return crawledAt == null ? null : crawledAt.toLocalDate().toString();
    }

    public boolean isProcessed() {
        return processed;
    }

    public String getOriginNoticeId() {
        return originNoticeId;
    }
}
