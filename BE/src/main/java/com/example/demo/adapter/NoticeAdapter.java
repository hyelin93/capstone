package com.example.demo.adapter;

import com.example.demo.dto.Notice;
import com.example.demo.entity.NoticeEntity;
import org.springframework.stereotype.Component;

@Component
public class NoticeAdapter {
    public NoticeEntity toEntity(Notice notice) {
        return NoticeEntity.create(
                notice.title(),
                notice.url(),
                notice.content(),
                notice.department(),
                notice.keyword(),
                notice.crawledAt(),
                notice.processed(),
                notice.originNoticeId()
        );
    }

    public Notice toDto(NoticeEntity entity) {
        return new Notice(
                entity.getNoticeId(),
                entity.getTitle(),
                entity.getUrl(),
                entity.getContent(),
                entity.getDepartment(),
                entity.getKeyword(),
                entity.getCrawledAt(),
                entity.isProcessed(),
                entity.getOriginNoticeId()
        );
    }
}
