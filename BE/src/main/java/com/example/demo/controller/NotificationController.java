package com.example.demo.controller;

import com.example.demo.dto.RegisterPushTokenRequest;
import com.example.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/token")
    public String registerPushToken(@RequestBody RegisterPushTokenRequest request) {
        return notificationService.registerPushToken(request);
    }
}
