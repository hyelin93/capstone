package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DemoApplication {

    // Spring Boot 애플리케이션을 시작합니다.
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
