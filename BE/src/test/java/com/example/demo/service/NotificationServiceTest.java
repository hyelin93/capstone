package com.example.demo.service;

import com.example.demo.dto.Notice;
import com.example.demo.dto.RegisterPushTokenRequest;
import com.example.demo.entity.Keyword;
import com.example.demo.entity.User;
import com.example.demo.repository.KeywordRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final KeywordRepository keywordRepository = mock(KeywordRepository.class);
    private final FcmService fcmService = mock(FcmService.class);
    private final NotificationService notificationService = new NotificationService(
            userRepository,
            keywordRepository,
            fcmService
    );

    @Test
    void registersPushTokenForExistingUser() {
        RegisterPushTokenRequest request = new RegisterPushTokenRequest();
        request.setUsername("alice");
        request.setToken(" fcm-token ");
        User user = User.builder()
                .username("alice")
                .password("password")
                .build();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        String result = notificationService.registerPushToken(request);

        assertThat(result).isEqualTo("푸시 토큰 등록 성공");
        assertThat(user.getFcmToken()).isEqualTo("fcm-token");
    }

    @Test
    void rejectsBlankPushTokenRequest() {
        RegisterPushTokenRequest request = new RegisterPushTokenRequest();
        request.setUsername("alice");
        request.setToken(" ");

        assertThatThrownBy(() -> notificationService.registerPushToken(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400 BAD_REQUEST");
    }

    @Test
    void sendsNoticeNotificationWhenKeywordMatches() {
        Notice notice = notice("2026 장학 신청 안내");
        Keyword keyword = Keyword.builder()
                .word("장학")
                .build();
        User user = User.builder()
                .username("alice")
                .fcmToken("fcm-token")
                .build();
        when(keywordRepository.findAll()).thenReturn(List.of(keyword));
        when(userRepository.findByFcmTokenIsNotNull()).thenReturn(List.of(user));

        notificationService.sendNoticeIfKeywordMatched(notice);

        verify(fcmService).sendNotification("fcm-token", "새 공지사항 등록", notice.title());
    }

    @Test
    void doesNotSendNoticeNotificationWhenKeywordDoesNotMatch() {
        Notice notice = notice("2026 수강 신청 안내");
        Keyword keyword = Keyword.builder()
                .word("장학")
                .build();
        when(keywordRepository.findAll()).thenReturn(List.of(keyword));

        notificationService.sendNoticeIfKeywordMatched(notice);

        verifyNoInteractions(fcmService);
    }

    private Notice notice(String title) {
        return new Notice(
                null,
                title,
                "https://www.syu.ac.kr/blog/notice/",
                "",
                "학사지원팀",
                "학사",
                null,
                false,
                "notice"
        );
    }
}
