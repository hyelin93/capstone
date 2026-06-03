package com.example.demo.entity;

import com.example.demo.dto.Notice;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "notice")
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

    // JPA가 공지 엔티티를 생성할 때 사용하는 기본 생성자입니다.
    protected NoticeEntity() {
    }

    // 크롤링된 공지 정보를 엔티티 필드에 초기화합니다.
    private NoticeEntity(
            String title,
            String url,
            String content,
            String department,
            String keyword,
            String originNoticeId
    ) {
        this.title = title;
        this.url = url;
        this.content = content;
        this.department = department;
        this.keyword = keyword;
        this.originNoticeId = originNoticeId;
        this.processed = false;
    }

    // 크롤링된 Notice DTO를 공지 엔티티로 변환합니다.
    public static NoticeEntity from(Notice notice) {
        return new NoticeEntity(
                notice.title(),
                notice.url(),
                "",
                resolveDepartment(notice),
                resolveKeyword(notice),
                notice.id()
        );
    }

    // 저장된 공지 엔티티를 API 응답용 Notice DTO로 변환합니다.
    public Notice toDto() {
        return new Notice(
                originNoticeId,
                title,
                keyword,
                department,
                null,
                url,
                keyword
        );
    }

    @PrePersist
    // 저장 직전에 크롤링 시각을 기본값으로 설정합니다.
    void onCreate() {
        if (crawledAt == null) {
            crawledAt = LocalDateTime.now();
        }
    }

    // 크롤링 결과에서 부서명을 우선순위에 따라 결정합니다.
    private static String resolveDepartment(Notice notice) {
        if (notice.author() != null && !notice.author().isBlank()) {
            return notice.author();
        }

        if (notice.category() != null && !notice.category().isBlank()) {
            return notice.category();
        }

        return "";
    }

    // 크롤링한 게시판명을 저장용 키워드 값으로 변환합니다.
    private static String resolveKeyword(Notice notice) {
        String boardName = firstPresent(notice.category(), notice.source());
        return switch (boardName) {
            case "학사공지", "학사" -> "학사";
            case "행사공지", "행사" -> "행사";
            case "생활공지", "생활" -> "생활";
            case "취업·창업공지", "취업창업공지", "취창업" -> "취창업";
            case "외부공지", "외부" -> "외부";
            case "추천채용" -> "추천채용";
            case "채용공고" -> "채용공고";
            default -> boardName;
        };
    }

    // 전달받은 문자열 중 비어 있지 않은 첫 번째 값을 반환합니다.
    private static String firstPresent(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }

        if (second != null && !second.isBlank()) {
            return second;
        }

        return "";
    }

    // 공지사항 번호를 반환합니다.
    public Integer getNoticeId() {
        return noticeId;
    }

    // 공지 제목을 반환합니다.
    public String getTitle() {
        return title;
    }

    // 공지 링크를 반환합니다.
    public String getUrl() {
        return url;
    }

    // 공지 본문을 반환합니다.
    public String getContent() {
        return content;
    }

    // 공지 담당 부서를 반환합니다.
    public String getDepartment() {
        return department;
    }

    // 공지가 속한 게시판 기반 키워드를 반환합니다.
    public String getKeyword() {
        return keyword;
    }

    // 공지를 크롤링한 시각을 반환합니다.
    public LocalDateTime getCrawledAt() {
        return crawledAt;
    }

    // 알림 처리 여부를 반환합니다.
    public boolean isProcessed() {
        return processed;
    }

    // 학교 홈페이지의 원본 공지 번호를 반환합니다.
    public String getOriginNoticeId() {
        return originNoticeId;
    }
}
