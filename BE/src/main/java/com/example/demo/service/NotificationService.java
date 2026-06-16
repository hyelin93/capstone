package com.example.demo.service;

import com.example.demo.dto.Notice;
import com.example.demo.dto.RegisterPushTokenRequest;
import com.example.demo.entity.Keyword;
import com.example.demo.entity.User;
import com.example.demo.repository.KeywordRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final String DEFAULT_NOTICE_TITLE = "새 공지사항 등록";

    private final UserRepository userRepository;
    private final KeywordRepository keywordRepository;
    private final FcmService fcmService;

    @Transactional
    public String registerPushToken(RegisterPushTokenRequest request) {
        if (request == null || !hasText(request.getUsername()) || !hasText(request.getToken())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username과 token은 필수입니다.");
        }

        User user = userRepository.findByUsername(request.getUsername().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        user.setFcmToken(request.getToken().trim());
        return "푸시 토큰 등록 성공";
    }

    @Transactional(readOnly = true)
    public void sendNoticeIfKeywordMatched(Notice notice) {
        if (notice == null || !hasText(notice.title())) {
            return;
        }

        boolean matched = keywordRepository.findAll().stream()
                .map(Keyword::getWord)
                .filter(this::hasText)
                .anyMatch(word -> notice.title().contains(word));

        if (!matched) {
            return;
        }

        List<User> recipients = userRepository.findByFcmTokenIsNotNull().stream()
                .filter(user -> hasText(user.getFcmToken()))
                .toList();

        for (User recipient : recipients) {
            fcmService.sendNotification(
                    recipient.getFcmToken(),
                    DEFAULT_NOTICE_TITLE,
                    notice.title()
            );
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
