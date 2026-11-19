package Eojinn.FC1878.ranking;

import Eojinn.FC1878.ranking.RankingService.RankingEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/api/rankings")
    public List<RankingEntry> top(@RequestParam(defaultValue = "50") int size) {
        return rankingService.top(Math.min(size, 100));
    }

    @GetMapping("/api/members/me/rank")
    public Map<String, Object> myRank(@AuthenticationPrincipal Long memberId) {
        return Map.of("rank", rankingService.myRank(memberId).map(Object.class::cast).orElse("-"));
    }
}
