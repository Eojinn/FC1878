package Eojinn.FC1878.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;

/** API-Football v3 응답 중 필요한 필드만 매핑 (https://www.api-football.com/documentation-v3) */
public final class ApiFootballDtos {
    private ApiFootballDtos() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ApiResponse<T>(List<T> response) {
    }

    // ---- /fixtures ----
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FixtureItem(FixtureInfo fixture, LeagueInfo league, Teams teams, Goals goals, Score score) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FixtureInfo(Integer id, OffsetDateTime date, Status status) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Status(@JsonProperty("short") String shortCode, Integer elapsed) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LeagueInfo(Integer id, String name, String country, String logo, Integer season, String round,
                             List<List<StandingItem>> standings) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Teams(TeamInfo home, TeamInfo away) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TeamInfo(Integer id, String name, String logo) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Goals(Integer home, Integer away) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Score(Goals penalty) {
    }

    // ---- /standings ----
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StandingsItem(LeagueInfo league) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StandingItem(int rank, TeamInfo team, int points, String form, WinLoss all) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WinLoss(int played, int win, int draw, int lose, GoalStat goals) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GoalStat(@JsonProperty("for") int goalsFor, @JsonProperty("against") int goalsAgainst) {
    }
}
