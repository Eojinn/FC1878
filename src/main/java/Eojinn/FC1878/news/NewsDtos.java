package Eojinn.FC1878.news;

import java.time.LocalDateTime;
import java.util.List;

public final class NewsDtos {

    private NewsDtos() {
    }

    /** sample: 키가 없어서 샘플 기사를 보여주는 중이면 true */
    public record NewsPageResponse(String topic, String query, List<TopicOption> topics, List<NewsItem> items,
                                   int start, boolean hasMore, boolean sample) {
    }

    public record TopicOption(String key, String label) {
    }

    /** publishedAt 은 한국 시간 */
    public record NewsItem(String title, String summary, String link, String originalLink, String source,
                           LocalDateTime publishedAt) {
    }

    /* ===== 네이버 API 허브 응답 ===== */

    public record NaverNewsResponse(Integer total, Integer start, Integer display, List<NaverNewsItem> items) {
    }

    public record NaverNewsItem(String title, String originallink, String link, String description, String pubDate) {
    }
}
