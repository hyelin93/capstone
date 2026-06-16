package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterPushTokenRequest {
    private String username;
    private String token;
}
