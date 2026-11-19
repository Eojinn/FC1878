package Eojinn.FC1878.football.dto;

import Eojinn.FC1878.football.domain.Fixture;
import Eojinn.FC1878.football.domain.League;
import Eojinn.FC1878.football.domain.Standing;
import Eojinn.FC1878.football.domain.Team;

import java.time.LocalDateTime;
import java.util.List;

public final class FootballDtos {
    private FootballDtos() {
    }

    public record LeagueResponse(Long id, Integer externalId, String name, String country, String logoUrl) {
        public static LeagueResponse from(League l) {
            return new LeagueResponse(l.getId(), l.getExternalId(), l.getDisplayName(), l.getCountry(), l.getLogoUrl());
        }
    }

    public record TeamResponse(Long id, String name, String logoUrl) {
        public static TeamResponse from(Team t) {
            return new TeamResponse(t.getId(), t.getDisplayName(), t.getLogoUrl());
        }
    }

    public record FixtureResponse(Long id, LeagueResponse league, String round, LocalDateTime kickoffAt,
                                  String status, Integer elapsed,
                                  TeamResponse home, TeamResponse away,
                                  Integer homeScore, Integer awayScore,
                                  Integer homePenalty, Integer awayPenalty) {
        public static FixtureResponse from(Fixture f) {
            return new FixtureResponse(f.getId(), LeagueResponse.from(f.getLeague()), f.getRound(), f.getKickoffAt(),
                    f.getStatus().name(), f.getElapsed(),
                    TeamResponse.from(f.getHomeTeam()), TeamResponse.from(f.getAwayTeam()),
                    f.getHomeScore(), f.getAwayScore(), f.getHomePenalty(), f.getAwayPenalty());
        }
    }

    /** 홈 화면처럼 리그별로 묶어서 보여주기 위한 응답 */
    public record LeagueFixturesResponse(LeagueResponse league, List<FixtureResponse> fixtures) {
    }

    public record StandingResponse(int rank, TeamResponse team, int played, int win, int draw, int lose,
                                   int goalsFor, int goalsAgainst, int goalDiff, int points, String form) {
        public static StandingResponse from(Standing s) {
            return new StandingResponse(s.getRank(), TeamResponse.from(s.getTeam()), s.getPlayed(), s.getWin(),
                    s.getDraw(), s.getLose(), s.getGoalsFor(), s.getGoalsAgainst(), s.getGoalDiff(),
                    s.getPoints(), s.getForm());
        }
    }
}
