package com.example.demo.entity;

import com.example.demo.dto.Notice;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeEntityTest {
    @Test
    // 크롤링한 게시판명을 저장용 키워드 값으로 변환하는지 검증합니다.
    void mapsBoardNamesToKeywords() {
        Map<String, String> expectedKeywords = Map.of(
                "학사공지", "학사",
                "행사공지", "행사",
                "생활공지", "생활",
                "취업·창업공지", "취창업",
                "외부공지", "외부",
                "추천채용", "추천채용",
                "채용공고", "채용공고"
        );

        expectedKeywords.forEach((boardName, keyword) ->
                assertThat(NoticeEntity.from(notice(boardName)).getKeyword()).isEqualTo(keyword)
        );
    }

    // 테스트에서 사용할 게시판별 공지 DTO를 생성합니다.
    private Notice notice(String boardName) {
        return new Notice(
                boardName,
                "공지 제목",
                boardName,
                "담당 부서",
                LocalDate.of(2026, 6, 3),
                "https://www.syu.ac.kr/blog/" + boardName + "/",
                boardName
        );
    }
}
