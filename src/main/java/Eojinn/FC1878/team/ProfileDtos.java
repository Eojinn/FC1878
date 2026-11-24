package Eojinn.FC1878.team;

import Eojinn.FC1878.football.dto.FootballDtos.FixtureResponse;
import Eojinn.FC1878.football.dto.FootballDtos.LeagueResponse;
import Eojinn.FC1878.football.dto.FootballDtos.TeamResponse;

import java.time.LocalDate;
import java.util.List;

public final class ProfileDtos {

    private ProfileDtos() {
    }

    /* ===== 팀 페이지 ===== */

    /** info 는 API 키가 없거나 불러오기 실패면 null. statSeason: 선수 기록 시즌 (2026 = 2026-27) */
    public record TeamProfileResponse(TeamResponse team, String englishName, TeamInfo info,
                                      List<TeamStanding> standings, List<FixtureResponse> recent,
                                      List<FixtureResponse> upcoming, List<SquadMember> squad, Integer statSeason) {
    }

    public record TeamInfo(String venue, Integer founded, String coach, String coachNationality, String clubColors,
                           String website, String address) {
    }

    public record TeamStanding(LeagueResponse league, Integer season, int rank, int played, int win, int draw,
                               int lose, int goalsFor, int goalsAgainst, int goalDiff, int points, String form) {
    }

    /** playerId 가 null 이면 선수 페이지가 없는 선수 (DB 동기화 전, API 에서 바로 받은 스쿼드) */
    public record SquadMember(Long playerId, String name, String koreanName, Integer number, String role, String position,
                              String nationality, Integer age, Integer played, Integer goals, Integer assists) {
    }

    /* ===== 선수 페이지 ===== */

    public record PlayerProfileResponse(Long id, String name, String koreanName, String role, String position, Integer number,
                                        String nationality, LocalDate dateOfBirth, Integer age, TeamResponse team,
                                        String teamName, String teamLogoUrl, List<PlayerSeasonStat> stats,
                                        List<RecentTransfer> transfers) {
    }

    public record PlayerSeasonStat(LeagueResponse league, Integer season, Integer played, int goals, Integer assists,
                                   Integer penalties) {
    }

    public record RecentTransfer(String fromClub, String toClub, String type, LocalDate date, String feeText) {
    }
}
