package com.example.demo.repository;

import com.example.demo.entity.NoticeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface NoticeRepository extends JpaRepository<NoticeEntity, Integer> {
    // 저장된 공지 목록을 크롤링 시각과 번호 기준으로 최신순 조회합니다.
    List<NoticeEntity> findAllByOrderByCrawledAtDescNoticeIdDesc();

    // 전달받은 URL 목록 중 이미 저장된 URL 집합을 조회합니다.
    @Query("select n.url from NoticeEntity n where n.url in :urls")
    Set<String> findExistingUrls(@Param("urls") Collection<String> urls);
}