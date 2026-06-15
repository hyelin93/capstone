package com.example.demo.adapter;

import com.example.demo.dto.Notice;
import com.example.demo.entity.NoticeEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeAdapterTest {
    private final NoticeAdapter noticeAdapter = new NoticeAdapter();

    @Test
    // Notice DTO의 필드를 공지 엔티티로 변환하는지 검증합니다.
    void convertsNoticeDtoToEntity() {
        Notice notice = notice();

        NoticeEntity entity = noticeAdapter.toEntity(notice);

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

    @Test
    // 공지 엔티티의 필드를 Notice DTO로 변환하는지 검증합니다.
    void convertsEntityToNoticeDto() {
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

        Notice notice = noticeAdapter.toDto(entity);

        assertThat(notice)
                .extracting(
                        Notice::title,
                        Notice::url,
                        Notice::content,
                        Notice::department,
                        Notice::keyword,
                        Notice::crawledAt,
                        Notice::processed,
                        Notice::originNoticeId
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
