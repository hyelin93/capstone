package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.SignupRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public String signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return "이미 존재하는 아이디입니다.";
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .build();

        userRepository.save(user);

        return "회원가입 성공";
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);

        if (user == null) {
            return "존재하지 않는 아이디입니다.";
        }

        if (!user.getPassword().equals(request.getPassword())) {
            return "비밀번호가 틀렸습니다.";
        }

        return "로그인 성공";
    }
}