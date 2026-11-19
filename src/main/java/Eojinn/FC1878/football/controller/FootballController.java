package Eojinn.FC1878.football.controller;

import Eojinn.FC1878.external.FootballApiProperties;
import Eojinn.FC1878.football.dto.FootballDtos.*;
import Eojinn.FC1878.football.service.FootballQueryService;
import Eojinn.FC1878.global.common.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FootballController {

    private final FootballQueryService queryService;
    private final FootballApiProperties apiProperties;

    @GetMapping("/leagues")
    public List<LeagueResponse> leagues() {
        return queryService.leagues();
    }

    /** 경기일정: GET /api/fixtures?date=2026-11-22&leagueId=1&teamId=3 (date 생략 시 오늘) */
    @GetMapping("/fixtures")
    public List<LeagueFixturesResponse> fixtures(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long leagueId,
            @RequestParam(required = false) Long teamId) {
        LocalDate target = date != null ? date : LocalDate.now(TimeUtil.KST);
        return queryService.fixturesByDate(target, leagueId, teamId);
    }

    @GetMapping("/fixtures/{id}")
    public FixtureResponse fixture(@PathVariable Long id) {
        return queryService.fixture(id);
    }

    /** 순위표: GET /api/leagues/1/standings?season=2026 */
    @GetMapping("/leagues/{leagueId}/standings")
    public List<StandingResponse> standings(@PathVariable Long leagueId,
                                            @RequestParam(required = false) Integer season) {
        return queryService.standings(leagueId, season != null ? season : apiProperties.season());
    }
}
