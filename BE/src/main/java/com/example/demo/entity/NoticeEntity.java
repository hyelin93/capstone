package com.example.demo.entity;

import com.example.demo.dto.Notice;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "notices",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_notices_url", columnNames = "url")
        },
        indexes = {
                @Index(name = "idx_notices_published_date", columnList = "published_date")
        }
)
public class NoticeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, length = 255)
    private String externalId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(length = 100)
    private String author;

    @Column(name = "published_date")
    private LocalDate publishedDate;

    @Column(nullable = false, length = 1000)
    private String url;

    @Column(nullable = false, length = 100)
    private String source;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected NoticeEntity() {
    }

    private NoticeEntity(
            String externalId,
            String title,
            String category,
            String author,
            LocalDate publishedDate,
            String url,
            String source
    ) {
        this.externalId = externalId;
        this.title = title;
        this.category = category;
        this.author = author;
        this.publishedDate = publishedDate;
        this.url = url;
        this.source = source;
    }

    public static NoticeEntity from(Notice notice) {
        return new NoticeEntity(
                notice.id(),
                notice.title(),
                notice.category(),
                notice.author(),
                notice.publishedDate(),
                notice.url(),
                notice.source()
        );
    }

    public Notice toDto() {
        return new Notice(
                externalId,
                title,
                category,
                author,
                publishedDate,
                url,
                source
        );
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getAuthor() {
        return author;
    }

    public LocalDate getPublishedDate() {
        return publishedDate;
    }

    public String getUrl() {
        return url;
    }

    public String getSource() {
        return source;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
