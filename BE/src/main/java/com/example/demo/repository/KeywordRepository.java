package com.example.demo.repository;

import com.example.demo.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    boolean existsByWord(String word);
}