package com.example.demo.repository;

import com.example.demo.entity.NoticeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface NoticeRepository extends JpaRepository<NoticeEntity, Integer> {
    List<NoticeEntity> findAllByOrderByCrawledAtDescNoticeIdDesc();

    List<NoticeEntity> findByUrlIn(Collection<String> urls);

    @Query("select n.url from NoticeEntity n where n.url in :urls")
    Set<String> findExistingUrls(@Param("urls") Collection<String> urls);
}
