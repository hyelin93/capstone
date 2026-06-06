package com.example.demo.entity;

import com.example.demo.dto.Notice;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "notice")
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

    // 크롤링된 공지 정보를 엔티티 필드에 초기화합니다.
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

    // 크롤링된 Notice DTO를 공지 엔티티로 변환합니다.
    public static NoticeEntity from(Notice notice) {
        return new NoticeEntity(
                notice.title(),
                notice.url(),
                notice.content(),
                notice.department(),
                notice.keyword(),
                notice.crawledAt(),
                notice.processed(),
                notice.originNoticeId()
        );
    }

    // 저장된 공지 엔티티를 API 응답용 Notice DTO로 변환합니다.
    public Notice toDto() {
        return new Notice(
                noticeId,
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

    @PrePersist
    // 저장 직전에 크롤링 시각을 기본값으로 설정합니다.
    void onCreate() {
        if (crawledAt == null) {
            crawledAt = LocalDateTime.now();
        }
    }
}
