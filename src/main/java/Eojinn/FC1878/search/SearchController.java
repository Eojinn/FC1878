package Eojinn.FC1878.search;

import Eojinn.FC1878.search.SearchService.SearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 팀·선수 검색: GET /api/search?q=아스널&limit=5 */
@RestController
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/api/search")
    public SearchResponse search(@RequestParam(defaultValue = "") String q,
                                 @RequestParam(defaultValue = "20") int limit) {
        return searchService.search(q, limit);
    }
}
