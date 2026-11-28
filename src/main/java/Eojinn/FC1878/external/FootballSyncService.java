package Eojinn.FC1878.external;

import Eojinn.FC1878.external.FootballDataDtos.*;
import Eojinn.FC1878.football.domain.*;
import Eojinn.FC1878.football.dto.FootballDtos.FixtureResponse;
import Eojinn.FC1878.football.repository.FixtureRepository;
import Eojinn.FC1878.football.repository.LeagueRepository;
import Eojinn.FC1878.football.repository.StandingRepository;
import Eojinn.FC1878.football.repository.TeamRepository;
import Eojinn.FC1878.global.common.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/** football-data.org 데이터를 DB에 upsert 하고, 변경된 경기는 WebSocket 으로 푸시 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FootballSyncService {

    /** 대회 코드 → 화면용 한글 이름 */
    private static final Map<String, String> LEAGUE_KO = Map.of(
            "PL", "프리미어리그", "PD", "라리가", "SA", "세리에A", "BL1", "분데스리가",
            "FL1", "리그1", "CL", "챔피언스리그", "DED", "에레디비시", "PPL", "프리메이라리가",
            "ELC", "챔피언십", "EC", "유로");

    private final FootballDataClient client;
    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final FixtureRepository fixtureRepository;
    private final StandingRepository standingRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /** from~to 기간 경기 동기화 (요청 1회) */
    @Transactional
    public int syncFixtures(LocalDate from, LocalDate to) {
        int count = 0;
        var matches = client.matches(from, to);
        log.info("football-data.org 경기 {}건 수신 ({} ~ {})", matches.size(), from, to);
        for (Match m : matches) {
            if (m.homeTeam() == null || m.homeTeam().id() == null
                    || m.awayTeam() == null || m.awayTeam().id() == null) {
                continue; // 토너먼트 대진 미정 경기
            }
            League league = upsertLeague(m.competition(), m.area());
            Team home = upsertTeam(m.homeTeam());
            Team away = upsertTeam(m.awayTeam());
            Integer seasonYear = m.season() != null && m.season().startYear() != null
                    ? m.season().startYear() : m.utcDate().getYear();

            // 새 경기는 킥오프 시간·상태를 모두 채운 뒤에 저장해야 함 (kickoff_at 은 NOT NULL)
            Fixture fixture = fixtureRepository.findByExternalId(m.id()).orElse(null);
            boolean isNew = fixture == null;
            if (isNew) {
                fixture = Fixture.of(m.id(), league, home, away, seasonYear);
            }

            Goals score = currentScore(m.score());
            Goals pen = m.score() == null ? null : m.score().penalties();

            boolean changed = fixture.updateState(
                    roundOf(m),
                    m.utcDate().atZoneSameInstant(TimeUtil.KST).toLocalDateTime(),
                    statusOf(m.status()),
                    m.minuteValue(),
                    score == null ? null : score.home(), score == null ? null : score.away(),
                    pen == null ? null : pen.home(), pen == null ? null : pen.away());
            if (isNew) {
                fixture = fixtureRepository.save(fixture);
            }

            if (changed && fixture.getId() != null) {
                FixtureResponse payload = FixtureResponse.from(fixture);
                messagingTemplate.convertAndSend("/topic/fixtures/" + fixture.getId(), payload);
                messagingTemplate.convertAndSend("/topic/live", payload);
            }
            count++;
        }
        return count;
    }

    /** 대회 순위표 동기화 (요청 1회). 리그 대회는 TOTAL 표만 저장 */
    @Transactional
    public void syncStandings(String competitionCode) {
        StandingsResponse res = client.standings(competitionCode);
        if (res == null || res.standings() == null) return;

        League league = upsertLeague(res.competition(), res.area());
        Integer season = res.season() != null ? res.season().startYear() : null;
        if (season == null) return;

        for (StandingGroup group : res.standings()) {
            if (!"TOTAL".equals(group.type()) || group.table() == null) continue;
            for (TableRow row : group.table()) {
                Team team = upsertTeam(row.team());
                Standing standing = standingRepository.findByLeagueIdAndSeasonAndTeamId(league.getId(), season, team.getId())
                        .orElseGet(() -> standingRepository.save(Standing.of(league, team, season)));
                standing.update(row.position(), row.playedGames(), row.won(), row.draw(), row.lost(),
                        row.goalsFor(), row.goalsAgainst(), row.points(), normalizeForm(row.form()));
            }
        }
    }

    /**
     * 최근 5경기 승무패 계산 (football-data 무료 순위표는 form 이 비어 있어서 끝난 경기로 직접 계산).
     * - 팀: 모든 대회를 합친 최근 5경기 → team.recent_form (경기일정·경기 상세에 표시)
     * - 순위표: 그 리그 경기만의 최근 5경기 → standing.form
     * 결과는 왼쪽이 오래된 경기, 오른쪽이 가장 최근 경기.
     *
     * @param finishedByCompetition 대회 코드 → 이번 시즌 끝난 경기
     */
    @Transactional
    public int syncForms(Map<String, List<Match>> finishedByCompetition) {
        Map<Integer, List<Result>> all = new HashMap<>();
        for (Map.Entry<String, List<Match>> e : finishedByCompetition.entrySet()) {
            Map<Integer, List<Result>> inLeague = new HashMap<>();
            Integer leagueExtId = null;
            Integer season = null;
            for (Match m : e.getValue()) {
                if (m.homeTeam() == null || m.awayTeam() == null || m.homeTeam().id() == null
                        || m.awayTeam().id() == null || m.utcDate() == null) continue;
                char home = resultOf(m.score(), true);
                if (home == 0) continue;
                char away = home == 'W' ? 'L' : home == 'L' ? 'W' : 'D';
                for (Map<Integer, List<Result>> target : List.of(all, inLeague)) {
                    target.computeIfAbsent(m.homeTeam().id(), k -> new ArrayList<>()).add(new Result(m.utcDate(), home));
                    target.computeIfAbsent(m.awayTeam().id(), k -> new ArrayList<>()).add(new Result(m.utcDate(), away));
                }
                if (m.competition() != null) leagueExtId = m.competition().id();
                if (m.season() != null && m.season().startYear() != null) season = m.season().startYear();
            }
            // 순위표의 최근 5경기 (그 리그 경기만)
            League league = leagueExtId == null ? null : leagueRepository.findByExternalId(leagueExtId).orElse(null);
            if (league != null && season != null) {
                for (Map.Entry<Integer, List<Result>> t : inLeague.entrySet()) {
                    Team team = teamRepository.findByExternalId(t.getKey()).orElse(null);
                    if (team == null) continue;
                    String form = lastFive(t.getValue());
                    standingRepository.findByLeagueIdAndSeasonAndTeamId(league.getId(), season, team.getId())
                            .ifPresent(s -> s.changeForm(form));
                }
            }
        }
        // 팀의 최근 5경기 (모든 대회)
        int n = 0;
        for (Map.Entry<Integer, List<Result>> t : all.entrySet()) {
            Team team = teamRepository.findByExternalId(t.getKey()).orElse(null);
            if (team == null) continue;
            team.changeRecentForm(lastFive(t.getValue()));
            n++;
        }
        return n;
    }

    private record Result(java.time.OffsetDateTime at, char wdl) {
    }

    /** 날짜순으로 정렬해 마지막 5경기 → "WDLWW" (왼쪽이 오래된 경기) */
    private static String lastFive(List<Result> results) {
        List<Result> sorted = new ArrayList<>(results);
        sorted.sort(Comparator.comparing(Result::at));
        StringBuilder sb = new StringBuilder();
        for (Result r : sorted.subList(Math.max(0, sorted.size() - 5), sorted.size())) sb.append(r.wdl());
        return sb.toString();
    }

    /** 홈 팀 기준 승(W)·무(D)·패(L). 승부차기까지 반영된 winner 를 먼저 보고, 없으면 최종 스코어로. 알 수 없으면 0 */
    private static char resultOf(Score s, boolean home) {
        if (s == null) return 0;
        if (s.winner() != null) {
            return switch (s.winner()) {
                case "HOME_TEAM" -> home ? 'W' : 'L';
                case "AWAY_TEAM" -> home ? 'L' : 'W';
                case "DRAW" -> 'D';
                default -> 0;
            };
        }
        Goals g = s.fullTime();
        if (g == null || g.home() == null || g.away() == null) return 0;
        int diff = home ? g.home() - g.away() : g.away() - g.home();
        return diff > 0 ? 'W' : diff < 0 ? 'L' : 'D';
    }

    // ---------- 변환 ----------

    static FixtureStatus statusOf(String status) {
        if (status == null) return FixtureStatus.SCHEDULED;
        return switch (status) {
            case "IN_PLAY", "PAUSED", "EXTRA_TIME", "PENALTY_SHOOTOUT", "LIVE" -> FixtureStatus.LIVE;
            case "FINISHED", "AWARDED" -> FixtureStatus.FINISHED;
            case "POSTPONED", "SUSPENDED" -> FixtureStatus.POSTPONED;
            case "CANCELLED" -> FixtureStatus.CANCELLED;
            default -> FixtureStatus.SCHEDULED; // SCHEDULED, TIMED
        };
    }

    /** 승부차기가 있으면 정규+연장 스코어, 아니면 fullTime (진행 중이면 현재 스코어) */
    private static Goals currentScore(Score s) {
        if (s == null) return null;
        if (s.regularTime() != null && s.regularTime().home() != null) {
            int h = s.regularTime().home(), a = s.regularTime().away();
            if (s.extraTime() != null && s.extraTime().home() != null) {
                h += s.extraTime().home();
                a += s.extraTime().away();
            }
            return new Goals(h, a);
        }
        return s.fullTime();
    }

    /** 리그 경기는 프론트가 "13라운드"로 바꿔 보여주는 형식으로 저장 */
    private static String roundOf(Match m) {
        String stage = m.stage();
        if (stage == null || "REGULAR_SEASON".equals(stage) || "LEAGUE_STAGE".equals(stage)) {
            return m.matchday() == null ? null : "Regular Season - " + m.matchday();
        }
        return switch (stage) {
            case "PLAYOFFS" -> "플레이오프";
            case "LAST_16" -> "16강";
            case "QUARTER_FINALS" -> "8강";
            case "SEMI_FINALS" -> "4강";
            case "FINAL" -> "결승";
            default -> stage;
        };
    }

    /** "W,W,D,L,W" → "WWDLW" */
    private static String normalizeForm(String form) {
        if (form == null) return null;
        String f = form.replace(",", "").trim();
        return f.length() > 10 ? f.substring(f.length() - 10) : f;
    }

    private League upsertLeague(Competition c, Area area) {
        String country = area == null ? null : area.name();
        League league = leagueRepository.findByExternalId(c.id())
                .orElseGet(() -> {
                    League l = League.of(c.id(), c.name(), country, c.emblem());
                    l.changeKoreanName(LEAGUE_KO.get(c.code()));
                    return leagueRepository.save(l);
                });
        league.update(c.name(), country, c.emblem());
        return league;
    }

    private Team upsertTeam(TeamRef t) {
        Team team = teamRepository.findByExternalId(t.id())
                .orElseGet(() -> teamRepository.save(Team.of(t.id(), t.displayName(), t.crest())));
        team.update(t.displayName(), t.crest());
        // 한글로도 찾을 수 있게 (짧은 이름이 사전에 없으면 정식 이름으로)
        String ko = Eojinn.FC1878.football.service.TeamKoreanNames.namesFor(t.displayName());
        if (ko == null) ko = Eojinn.FC1878.football.service.TeamKoreanNames.namesFor(t.name());
        if (ko != null) team.changeSearchNames(ko);
        return team;
    }
}
