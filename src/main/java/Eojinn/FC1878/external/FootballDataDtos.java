package Eojinn.FC1878.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;
import java.util.List;

/** football-data.org v4 응답 중 필요한 필드만 매핑 (https://docs.football-data.org/general/v4/) */
public final class FootballDataDtos {
    private FootballDataDtos() {
    }

    // ---- 공통 ----
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Area(String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Competition(Integer id, String name, String code, String emblem) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TeamRef(Integer id, String name, String shortName, String tla, String crest) {
        /** "Manchester City FC" 보다 "Man City" 처럼 짧은 이름을 우선 사용 */
        public String displayName() {
            return shortName != null && !shortName.isBlank() ? shortName : name;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Season(String startDate) {
        public Integer startYear() {
            return startDate == null || startDate.length() < 4 ? null : Integer.valueOf(startDate.substring(0, 4));
        }
    }

    // ---- /v4/matches ----
    @JsonIgnoreProperties(ignoreUnknown = true)
    /** GET /competitions/{code}/teams 응답 (팀마다 squad 가 오면 그대로 사용) */
    public record CompetitionTeamsResponse(List<CompetitionTeam> teams) {
    }

    public record CompetitionTeam(Integer id, String name, String shortName, String crest, List<SquadPlayer> squad) {
    }

    /** GET /teams/{id} 응답 (팀 정보 + 스쿼드) */
    public record TeamDetail(Integer id, String name, String shortName, String crest, String address, String website,
                             Integer founded, String clubColors, String venue, Coach coach, List<SquadPlayer> squad) {
    }

    public record Coach(String name, String nationality) {
    }

    /** position 예: "Goalkeeper", "Centre-Back", "Defence", "Central Midfield", "Offence". dateOfBirth 예: "2001-09-05" */
    public record SquadPlayer(Integer id, String name, String position, String dateOfBirth, Integer shirtNumber,
                              String nationality) {
    }

    /** GET /competitions/{code}/scorers (득점 순위: 골·도움·페널티·출전 경기) */
    public record ScorersResponse(Competition competition, Season season, List<Scorer> scorers) {
    }

    public record Scorer(SquadPlayer player, TeamRef team, Integer playedMatches, Integer goals, Integer assists,
                         Integer penalties) {
    }

    public record MatchesResponse(List<Match> matches) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Match(Integer id, Area area, Competition competition, Season season, OffsetDateTime utcDate,
                        String status, Object minute, Integer matchday, String stage,
                        TeamRef homeTeam, TeamRef awayTeam, Score score) {
        /** minute 은 90 같은 숫자나 "45+2" 같은 문자열로 올 수 있음 → 앞쪽 숫자만 사용 */
        public Integer minuteValue() {
            if (minute instanceof Number n) return n.intValue();
            if (minute instanceof String s) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("^\\d+").matcher(s.trim());
                return m.find() ? Integer.valueOf(m.group()) : null;
            }
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    /** winner: HOME_TEAM · AWAY_TEAM · DRAW (승부차기까지 반영된 최종 승자) */
    public record Score(String duration, Goals fullTime, Goals regularTime, Goals extraTime, Goals penalties, String winner) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Goals(Integer home, Integer away) {
    }

    // ---- /v4/competitions/{code}/standings ----
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StandingsResponse(Area area, Competition competition, Season season, List<StandingGroup> standings) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StandingGroup(String stage, String type, String group, List<TableRow> table) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TableRow(int position, TeamRef team, int playedGames, String form, int won, int draw, int lost,
                           int points, int goalsFor, int goalsAgainst) {
    }
}
