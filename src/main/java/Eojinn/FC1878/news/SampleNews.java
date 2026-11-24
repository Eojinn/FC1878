package Eojinn.FC1878.news;

import Eojinn.FC1878.news.NewsDtos.NewsItem;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * 네이버 키가 없을 때 화면 확인용 샘플.
 * 실제 기사처럼 보이지 않도록 "샘플"이라고 적고, 누르면 네이버 뉴스 검색 결과로 이동.
 */
final class SampleNews {

    private SampleNews() {
    }

    static List<NewsItem> items(String query, int start) {
        if (start > 1) return List.of();
        String search = "https://search.naver.com/search.naver?where=news&sort=1&query="
                + URLEncoder.encode(query, StandardCharsets.UTF_8);
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul")).withSecond(0).withNano(0);
        List<NewsItem> list = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            list.add(new NewsItem(
                    "[샘플 " + i + "] '" + query + "' 기사 제목이 여기에 나와요",
                    "네이버 API 허브 키(NAVER_CLIENT_ID, NAVER_CLIENT_SECRET)를 넣으면 실제 기사 제목과 요약이 표시돼요. "
                            + "누르면 네이버 뉴스 검색 결과로 이동해요.",
                    search, null, "샘플", now.minusMinutes(i * 37L)));
        }
        return list;
    }
}
