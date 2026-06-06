package com.example.demo.entity;

import com.example.demo.dto.Notice;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeEntityTest {
    @Test
    // Notice DTO의 필드를 공지 엔티티로 복사하는지 검증합니다.
    void copiesNoticeDtoFields() {
        Notice notice = notice();

        NoticeEntity entity = NoticeEntity.from(notice);

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

    // 테스트에서 사용할 공지 DTO를 생성합니다.
    private Notice notice() {
        return new Notice(
                null,
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
