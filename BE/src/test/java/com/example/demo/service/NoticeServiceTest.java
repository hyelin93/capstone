package com.example.demo.service;

import com.example.demo.crawler.NoticeCrawler;
import com.example.demo.dto.Notice;
import com.example.demo.entity.NoticeEntity;
import com.example.demo.repository.NoticeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {
    private final NoticeCrawler noticeCrawler = mock(NoticeCrawler.class);
    private final NoticeRepository noticeRepository = mock(NoticeRepository.class);
    private final NoticeService noticeService = new NoticeService(noticeCrawler, noticeRepository);

    @Test
    @SuppressWarnings("unchecked")
    // 이미 등록된 공지는 제외하고 신규 공지만 저장하는지 검증합니다.
    void savesOnlyNoticesThatHaveNotBeenRegistered() {
        Notice alreadyRegistered = notice("same", "이미 저장된 공지", "https://www.syu.ac.kr/blog/same/");
        Notice newNotice = notice("new", "새 공지", "https://www.syu.ac.kr/blog/new/");
        when(noticeCrawler.crawlNoticeBoards()).thenReturn(List.of(alreadyRegistered, newNotice));
        when(noticeRepository.findExistingUrls(anyCollection())).thenReturn(Set.of(alreadyRegistered.url()));
        when(noticeRepository.findAllByOrderByCrawledAtDescNoticeIdDesc())
                .thenReturn(List.of(NoticeEntity.from(newNotice), NoticeEntity.from(alreadyRegistered)));

        List<Notice> notices = noticeService.getLatestNotices();

        ArgumentCaptor<List<NoticeEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(noticeRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getUrl()).isEqualTo(newNotice.url());
        assertThat(captor.getValue().get(0).getKeyword()).isEqualTo("학사");
        assertThat(notices).extracting(Notice::url)
                .containsExactly(newNotice.url(), alreadyRegistered.url());
    }

    @Test
    // 크롤링된 모든 공지가 이미 등록된 경우 추가 저장하지 않는지 검증합니다.
    void doesNotSaveWhenEveryCrawledNoticeAlreadyExists() {
        Notice alreadyRegistered = notice("same", "이미 저장된 공지", "https://www.syu.ac.kr/blog/same/");
        when(noticeCrawler.crawlNoticeBoards()).thenReturn(List.of(alreadyRegistered));
        when(noticeRepository.findExistingUrls(anyCollection())).thenReturn(Set.of(alreadyRegistered.url()));
        when(noticeRepository.findAllByOrderByCrawledAtDescNoticeIdDesc())
                .thenReturn(List.of(NoticeEntity.from(alreadyRegistered)));

        List<Notice> notices = noticeService.getLatestNotices();

        verify(noticeRepository).findExistingUrls(Set.of(alreadyRegistered.url()));
        verify(noticeRepository).findAllByOrderByCrawledAtDescNoticeIdDesc();
        verifyNoMoreInteractions(noticeRepository);
        assertThat(notices).hasSize(1);
    }

    // 테스트에서 사용할 공지 DTO를 생성합니다.
    private Notice notice(String id, String title, String url) {
        return new Notice(
                id,
                title,
                "학사공지",
                "학사지원팀",
                LocalDate.of(2026, 6, 1),
                url,
                "학사공지"
        );
    }
}
