package com.example.demo.service;

import com.example.demo.entity.Keyword;
import com.example.demo.repository.KeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeywordService {

    private final KeywordRepository keywordRepository;

    public Keyword addKeyword(String word) {
        if (keywordRepository.existsByWord(word)) {
            throw new RuntimeException("이미 등록된 키워드입니다.");
        }

        Keyword keyword = Keyword.builder()
                .word(word)
                .build();

        return keywordRepository.save(keyword);
    }

    public List<Keyword> getKeywords() {
        return keywordRepository.findAll();
    }

    public void deleteKeyword(Long id) {
        keywordRepository.deleteById(id);
    }
}