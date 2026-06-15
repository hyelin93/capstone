package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "notice",
        uniqueConstraints = @UniqueConstraint(name = "uk_notice_url", columnNames = "url"),
        indexes = @Index(name = "idx_notice_crawled_at_notice_id", columnList = "crawled_at, notice_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Integer noticeId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 512)
    private String url;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 50)
    private String department;

    @Column(nullable = false, length = 50)
    private String keyword;

    @Column(name = "crawled_at", nullable = false)
    private LocalDateTime crawledAt;

    @Column(name = "is_processed", nullable = false)
    private boolean processed;

    @Column(name = "origin_notice_id", nullable = false, length = 50)
    private String originNoticeId;

    private NoticeEntity(
            String title,
            String url,
            String content,
            String department,
            String keyword,
            LocalDateTime crawledAt,
            boolean processed,
            String originNoticeId
    ) {
        this.title = title;
        this.url = url;
        this.content = content;
        this.department = department;
        this.keyword = keyword;
        this.crawledAt = crawledAt;
        this.originNoticeId = originNoticeId;
        this.processed = processed;
    }

    public static NoticeEntity create(
            String title,
            String url,
            String content,
            String department,
            String keyword,
            LocalDateTime crawledAt,
            boolean processed,
            String originNoticeId
    ) {
        return new NoticeEntity(
                title,
                url,
                content,
                department,
                keyword,
                crawledAt,
                processed,
                originNoticeId
        );
    }

    public void updateContent(String content) {
        this.content = content;
    }

    @PrePersist
    void onCreate() {
        if (crawledAt == null) {
            crawledAt = LocalDateTime.now();
        }
    }
}
