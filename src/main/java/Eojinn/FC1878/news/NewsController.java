package Eojinn.FC1878.news;

import Eojinn.FC1878.news.NewsDtos.NewsPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 해외축구 뉴스: GET /api/news?topic=EPL&q=손흥민&start=1
 * - topic: ALL(기본), EPL, LALIGA, BUNDESLIGA, SERIE_A, LIGUE_1, UCL, OVERSEAS, TRANSFER
 * - q: 직접 검색 (있으면 topic 대신 사용)
 * - start: 다음 페이지는 1, 21, 41 … (한 번에 20개)
 */
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping
    public NewsPageResponse list(@RequestParam(required = false) String topic,
                                 @RequestParam(required = false) String q,
                                 @RequestParam(defaultValue = "1") int start) {
        return newsService.search(topic, q, start);
    }
}
