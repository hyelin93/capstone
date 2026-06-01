package com.example.demo.dto;

import java.time.LocalDate;

public record Notice(
        String id,
        String title,
        String category,
        String author,
        LocalDate publishedDate,
        String url,
        String source
) {
}
