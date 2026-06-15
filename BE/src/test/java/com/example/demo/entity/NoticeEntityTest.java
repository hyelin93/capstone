package com.example.demo.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeEntityTest {
    @Test
    // 전달받은 필드를 공지 엔티티로 초기화하는지 검증합니다.
    void createsNoticeEntity() {
        NoticeEntity entity = NoticeEntity.create(
                "공지 제목",
                "https://www.syu.ac.kr/blog/test-notice/",
                "공지 본문",
                "담당 부서",
                "학사",
                LocalDateTime.of(2026, 6, 3, 12, 0),
                false,
                "test-notice"
        );

        assertThat(entity)
                .extracting(
                        NoticeEntity::getTitle,
                        NoticeEntity::getUrl,
                        NoticeEntity::getContent,
                        NoticeEntity::getDepartment,
                        NoticeEntity::getKeyword,
                        NoticeEntity::getCrawledAt,
                        NoticeEntity::isProcessed,
                        NoticeEntity::getOriginNoticeId
                )
                .containsExactly(
                        "공지 제목",
                        "https://www.syu.ac.kr/blog/test-notice/",
                        "공지 본문",
                        "담당 부서",
                        "학사",
                        LocalDateTime.of(2026, 6, 3, 12, 0),
                        false,
                        "test-notice"
                );
    }
}
