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
                        Notice::id,
                        Notice::title,
                        Notice::category,
                        Notice::author,
                        Notice::url,
                        Notice::source
                )
                .containsExactly(
                        "test-notice",
                        "2026학년도 하계계절학기 폐강과목 공고",
                        "수업",
                        "학사지원팀",
                        "https://www.syu.ac.kr/blog/test-notice/",
                        "학사공지"
                );
        assertThat(notices.get(0).publishedDate()).hasToString("2026-05-29");
    }

    @Test
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
}
