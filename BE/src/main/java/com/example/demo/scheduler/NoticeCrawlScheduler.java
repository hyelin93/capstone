package com.example.demo.scheduler;

import com.example.demo.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeCrawlScheduler {
    private static final String HOURLY_CRON = "0 0 * * * *";
    private static final String SCHEDULE_ZONE = "Asia/Seoul";

    private final NoticeService noticeService;

    @Scheduled(cron = HOURLY_CRON, zone = SCHEDULE_ZONE)
    public void crawlNoticesHourly() {
        long startedAt = System.nanoTime();
        log.info("정기 공지 크롤링 시작: cron={}, zone={}", HOURLY_CRON, SCHEDULE_ZONE);

        try {
            noticeService.crawlAndSaveLatestNotices();
            log.info("정기 공지 크롤링 완료: cron={}, zone={}, elapsedMs={}", HOURLY_CRON, SCHEDULE_ZONE, elapsedMillis(startedAt));
        } catch (RuntimeException e) {
            log.error("정기 공지 크롤링 실패: cron={}, zone={}, elapsedMs={}", HOURLY_CRON, SCHEDULE_ZONE, elapsedMillis(startedAt), e);
            throw e;
        }
    }

    private long elapsedMillis(long startedAt) {
        return Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
    }
}
