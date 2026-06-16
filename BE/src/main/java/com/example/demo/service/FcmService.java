package com.example.demo.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FcmService {

    public void sendNotification(String token, String title, String body) {
        if (!hasText(token)) {
            log.warn("FCM 알림 발송 건너뜀: 토큰이 비어 있습니다.");
            return;
        }

        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 알림 발송 성공: response={}", response);
        } catch (FirebaseMessagingException e) {
            log.warn("FCM 알림 발송 실패: token={}", maskToken(token), e);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String maskToken(String token) {
        if (token.length() <= 8) {
            return "****";
        }

        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }
}
