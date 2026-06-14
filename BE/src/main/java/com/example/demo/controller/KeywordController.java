package com.example.demo.controller;

import com.example.demo.entity.Keyword;
import com.example.demo.service.KeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/keywords")
@RequiredArgsConstructor
public class KeywordController {

    private final KeywordService keywordService;

    @PostMapping
    public Keyword addKeyword(@RequestBody Map<String, String> request) {
        return keywordService.addKeyword(request.get("word"));
    }

    @GetMapping
    public List<Keyword> getKeywords() {
        return keywordService.getKeywords();
    }

    @DeleteMapping("/{id}")
    public String deleteKeyword(@PathVariable Long id) {
        keywordService.deleteKeyword(id);
        return "키워드 삭제 완료";
    }
}