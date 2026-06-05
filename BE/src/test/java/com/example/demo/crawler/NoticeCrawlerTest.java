package com.example.demo.crawler;

import com.example.demo.dto.Notice;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeCrawlerTest {
    private final NoticeCrawler noticeCrawler = new NoticeCrawler();

    @Test
    // 학사공지 목록 HTML 행을 Notice DTO로 파싱하는지 검증합니다.
    void parsesAcademicNoticeRows() {
        String html = """
                <table>
                  <tr>
                    <td>notice</td>
                    <td class="step2">
                      <a class="itembx" href="/blog/test-notice/?c=&amp;k=&amp;pageds=1&amp;t=">
                        <span class="md_cate">수업</span>
                        <span class="tit">2026학년도 하계계절학기 폐강과목 공고 NEW</span>
                      </a>
                    </td>
                    <td class="step3">학사지원팀</td>
                    <td class="step4">2026.05.29</td>
                    <td>file download</td>
                    <td>469</td>
                  </tr>
                </table>
                """;
        Document document = Jsoup.parse(html, "https://www.syu.ac.kr/academic/academic-notice/");

        List<Notice> notices = noticeCrawler.parseNoticeList(document, "학사공지");

        assertThat(notices).hasSize(1);
        assertThat(notices.get(0))
                .extracting(
                        Notice::noticeId,
                        Notice::title,
                        Notice::url,
                        Notice::content,
                        Notice::department,
                        Notice::keyword,
                        Notice::crawledAt,
                        Notice::processed,
                        Notice::originNoticeId
                )
                .containsExactly(
                        null,
                        "2026학년도 하계계절학기 폐강과목 공고",
                        "https://www.syu.ac.kr/blog/test-notice/",
                        "",
                        "학사지원팀",
                        "학사",
                        null,
                        false,
                        "test-notice"
                );
    }

    @Test
    // 쿼리 문자열만 다른 동일 공지 URL을 중복 제거하는지 검증합니다.
    void removesDuplicateRowsByCanonicalUrl() {
        String html = """
                <table>
                  <tr>
                    <td class="step2"><a class="itembx" href="/blog/same/?pageds=1"><span class="tit">첫 번째</span></a></td>
                    <td class="step3">학사지원팀</td>
                    <td class="step4">2026.06.01</td>
                  </tr>
                  <tr>
                    <td class="step2"><a class="itembx" href="/blog/same/?pageds=2"><span class="tit">두 번째</span></a></td>
                    <td class="step3">학사지원팀</td>
                    <td class="step4">2026.06.01</td>
                  </tr>
                </table>
                """;
        Document document = Jsoup.parse(html, "https://www.syu.ac.kr/academic/academic-notice/");

        List<Notice> notices = noticeCrawler.parseNoticeList(document, "학사공지");

        assertThat(notices).hasSize(1);
        assertThat(notices.get(0).title()).isEqualTo("첫 번째");
    }

    @Test
    // 게시판 이름을 Notice의 keyword 필드로 변환하는지 검증합니다.
    void usesBoardNameAsNoticeKeyword() {
        String html = """
                <table>
                  <tr>
                    <td class="step2">
                      <a class="itembx" href="/blog/event-notice/">
                        <span class="md_cate">부서행사</span>
                        <span class="tit">행사 안내</span>
                      </a>
                    </td>
                    <td class="step3">학생처</td>
                    <td class="step4">2026.06.01</td>
                  </tr>
                </table>
                """;
        Document document = Jsoup.parse(html, "https://www.syu.ac.kr/university-square/notice/event/");

        List<Notice> notices = noticeCrawler.parseNoticeList(document, "행사공지");

        assertThat(notices).hasSize(1);
        assertThat(notices.get(0).keyword()).isEqualTo("행사");
    }

    @Test
    // 문서에 정의된 게시판 이름을 저장용 키워드 값으로 변환하는지 검증합니다.
    void mapsBoardNamesToKeywords() {
        assertThat(noticeFromBoard("학사공지").keyword()).isEqualTo("학사");
        assertThat(noticeFromBoard("행사공지").keyword()).isEqualTo("행사");
        assertThat(noticeFromBoard("생활공지").keyword()).isEqualTo("생활");
        assertThat(noticeFromBoard("취업·창업공지").keyword()).isEqualTo("취창업");
        assertThat(noticeFromBoard("외부공지").keyword()).isEqualTo("외부");
        assertThat(noticeFromBoard("추천채용").keyword()).isEqualTo("추천채용");
        assertThat(noticeFromBoard("채용공고").keyword()).isEqualTo("채용공고");
    }

    // 테스트에서 사용할 게시판별 공지 DTO를 파싱합니다.
    private Notice noticeFromBoard(String boardName) {
        String html = """
                <table>
                  <tr>
                    <td class="step2"><a class="itembx" href="/blog/test-notice/"><span class="tit">공지 제목</span></a></td>
                    <td class="step3">담당 부서</td>
                  </tr>
                </table>
                """;
        Document document = Jsoup.parse(html, "https://www.syu.ac.kr/");

        return noticeCrawler.parseNoticeList(document, boardName).get(0);
    }
}
