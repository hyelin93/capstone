package com.example.demo.service;

import com.example.demo.adapter.NoticeAdapter;
import com.example.demo.crawler.NoticeCrawler;
import com.example.demo.dto.Notice;
import com.example.demo.entity.NoticeEntity;
import com.example.demo.repository.NoticeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {
    private final NoticeCrawler noticeCrawler = mock(NoticeCrawler.class);
    private final NoticeRepository noticeRepository = mock(NoticeRepository.class);
    private final NoticeAdapter noticeAdapter = new NoticeAdapter();
    private final NotificationService notificationService = mock(NotificationService.class);
    private final NoticeService noticeService = new NoticeService(
            noticeCrawler,
            noticeRepository,
            noticeAdapter,
            notificationService
    );

    @Test
    @SuppressWarnings("unchecked")
    // 이미 등록된 공지는 제외하고 신규 공지만 저장하는지 검증합니다.
    void savesOnlyNoticesThatHaveNotBeenRegistered() {
        Notice alreadyRegistered = notice("same", "이미 저장된 공지", "https://www.syu.ac.kr/blog/same/");
        Notice newNotice = notice("new", "새 공지", "https://www.syu.ac.kr/blog/new/");
        when(noticeCrawler.crawlNoticeBoards()).thenReturn(List.of(alreadyRegistered, newNotice));
        when(noticeRepository.findExistingUrls(anyCollection())).thenReturn(Set.of(alreadyRegistered.url()));
        when(noticeRepository.findAllByOrderByCrawledAtDescNoticeIdDesc())
                .thenReturn(List.of(noticeAdapter.toEntity(newNotice), noticeAdapter.toEntity(alreadyRegistered)));

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
                .thenReturn(List.of(noticeAdapter.toEntity(alreadyRegistered)));

        List<Notice> notices = noticeService.getLatestNotices();

        verify(noticeRepository).findExistingUrls(Set.of(alreadyRegistered.url()));
        verify(noticeRepository).findAllByOrderByCrawledAtDescNoticeIdDesc();
        verifyNoMoreInteractions(noticeRepository);
        assertThat(notices).hasSize(1);
    }

    @Test
    // 기존 저장 공지의 본문이 비어 있으면 새 크롤링 결과의 본문으로 보강하는지 검증합니다.
    void fillsBlankContentForAlreadyRegisteredNotice() {
        Notice crawledNotice = notice(
                "same",
                "이미 저장된 공지",
                "https://www.syu.ac.kr/blog/same/",
                "새로 크롤링한 본문"
        );
        NoticeEntity savedEntity = NoticeEntity.create(
                crawledNotice.title(),
                crawledNotice.url(),
                "",
                crawledNotice.department(),
                crawledNotice.keyword(),
                null,
                false,
                crawledNotice.originNoticeId()
        );
        when(noticeCrawler.crawlNoticeBoards()).thenReturn(List.of(crawledNotice));
        when(noticeRepository.findExistingUrls(anyCollection())).thenReturn(Set.of(crawledNotice.url()));
        when(noticeRepository.findByUrlIn(Set.of(crawledNotice.url()))).thenReturn(List.of(savedEntity));
        when(noticeRepository.findAllByOrderByCrawledAtDescNoticeIdDesc()).thenReturn(List.of(savedEntity));

        List<Notice> notices = noticeService.getLatestNotices();

        assertThat(savedEntity.getContent()).isEqualTo("새로 크롤링한 본문");
        assertThat(notices).extracting(Notice::content).containsExactly("새로 크롤링한 본문");
    }

    @Test
    // 상세 조회 시 저장된 본문이 비어 있으면 원문 상세 페이지에서 본문을 채워 반환하는지 검증합니다.
    void fillsBlankContentWhenGettingNoticeDetail() {
        NoticeEntity savedEntity = NoticeEntity.create(
                "공지 제목",
                "https://www.syu.ac.kr/blog/detail/",
                "",
                "학사지원팀",
                "학사",
                null,
                false,
                "detail"
        );
        when(noticeRepository.findById(1)).thenReturn(Optional.of(savedEntity));
        when(noticeCrawler.crawlNoticeContent(savedEntity.getUrl())).thenReturn("상세 본문");

        Notice notice = noticeService.getNoticeDetail(1);

        assertThat(savedEntity.getContent()).isEqualTo("상세 본문");
        assertThat(notice.content()).isEqualTo("상세 본문");
    }

    @Test
    // 테스트용 장학금 공지를 생성하고 키워드 알림 매칭을 호출하는지 검증합니다.
    void createsTestNotice() {
        when(noticeRepository.save(any(NoticeEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notice notice = noticeService.createTestNotice();

        assertThat(notice.title()).isEqualTo("2026학년도 2학기 장학금 신청 안내");
        assertThat(notice.content()).contains("장학금 신청");
        assertThat(notice.department()).isEqualTo("학생지원팀");
        assertThat(notice.keyword()).isEqualTo("학사");
        assertThat(notice.url()).startsWith("https://www.syu.ac.kr/test/notices/test-notice-");
        verify(notificationService).sendNoticeIfKeywordMatched(notice);
    }

    // 테스트에서 사용할 공지 DTO를 생성합니다.
    private Notice notice(String id, String title, String url) {
        return notice(id, title, url, "");
    }

    private Notice notice(String id, String title, String url, String content) {
        return new Notice(
                null,
                title,
                url,
                content,
                "학사지원팀",
                "학사",
                null,
                false,
                id
        );
    }
}
