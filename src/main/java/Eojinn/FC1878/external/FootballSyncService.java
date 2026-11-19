package Eojinn.FC1878.external;

import Eojinn.FC1878.external.ApiFootballDtos.*;
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
import java.util.HashSet;
import java.util.Set;

/** 외부 API 데이터를 DB에 upsert 하고, 변경된 경기는 WebSocket 으로 푸시 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FootballSyncService {

    private final ApiFootballClient client;
    private final FootballApiProperties props;
    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final FixtureRepository fixtureRepository;
    private final StandingRepository standingRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void syncFixtures(LocalDate date) {
        Set<Integer> targetLeagues = new HashSet<>(props.leagueIds());

        for (FixtureItem item : client.fixturesByDate(date)) {
            if (!targetLeagues.contains(item.league().id())) continue;

            League league = upsertLeague(item.league());
            Team home = upsertTeam(item.teams().home());
            Team away = upsertTeam(item.teams().away());

            Fixture fixture = fixtureRepository.findByExternalId(item.fixture().id())
                    .orElseGet(() -> fixtureRepository.save(
                            Fixture.of(item.fixture().id(), league, home, away, item.league().season())));

            FixtureStatus status = FixtureStatus.fromApiCode(item.fixture().status().shortCode());
            Goals pen = item.score() == null ? null : item.score().penalty();

            boolean changed = fixture.updateState(
                    item.league().round(),
                    item.fixture().date().atZoneSameInstant(TimeUtil.KST).toLocalDateTime(),
                    status,
                    item.fixture().status().elapsed(),
                    item.goals().home(), item.goals().away(),
                    pen == null ? null : pen.home(), pen == null ? null : pen.away());

            if (changed && fixture.getId() != null) {
                FixtureResponse payload = FixtureResponse.from(fixture);
                messagingTemplate.convertAndSend("/topic/fixtures/" + fixture.getId(), payload);
                messagingTemplate.convertAndSend("/topic/live", payload);
            }
        }
    }

    @Transactional
    public void syncStandings(int leagueExternalId, int season) {
        League league = leagueRepository.findByExternalId(leagueExternalId).orElse(null);
        if (league == null) {
            log.info("league {} 가 아직 DB에 없어 순위 동기화를 건너뜀 (경기 동기화 후 다시 시도)", leagueExternalId);
            return;
        }
        for (StandingItem s : client.standings(leagueExternalId, season)) {
            Team team = upsertTeam(s.team());
            Standing standing = standingRepository.findByLeagueIdAndSeasonAndTeamId(league.getId(), season, team.getId())
                    .orElseGet(() -> standingRepository.save(Standing.of(league, team, season)));
            var all = s.all();
            standing.update(s.rank(), all.played(), all.win(), all.draw(), all.lose(),
                    all.goals().goalsFor(), all.goals().goalsAgainst(), s.points(), s.form());
        }
    }

    private League upsertLeague(LeagueInfo info) {
        League league = leagueRepository.findByExternalId(info.id())
                .orElseGet(() -> leagueRepository.save(League.of(info.id(), info.name(), info.country(), info.logo())));
        league.update(info.name(), info.country(), info.logo());
        return league;
    }

    private Team upsertTeam(TeamInfo info) {
        Team team = teamRepository.findByExternalId(info.id())
                .orElseGet(() -> teamRepository.save(Team.of(info.id(), info.name(), info.logo())));
        team.update(info.name(), info.logo());
        return team;
    }
}
