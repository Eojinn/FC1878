package Eojinn.FC1878.player;

import Eojinn.FC1878.external.FootballApiProperties;
import Eojinn.FC1878.external.FootballDataClient;
import Eojinn.FC1878.external.FootballDataDtos.Scorer;
import Eojinn.FC1878.external.FootballDataDtos.ScorersResponse;
import Eojinn.FC1878.football.domain.League;
import Eojinn.FC1878.football.domain.Team;
import Eojinn.FC1878.football.repository.LeagueRepository;
import Eojinn.FC1878.football.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

/**
 * 선수 기록 동기화: 서버 시작 1분 뒤, 이후 3시간마다 대회별 득점 순위(최대 100명)를 받아 저장.
 * 스쿼드 동기화 전이라도 선수 페이지가 열리도록, 처음 보는 선수는 이때 player 에도 넣어 둠.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnExpression("!'${football.api.key:}'.isBlank() and ${football.api.enabled:true}")
public class PlayerStatSyncService {

    private static final int LIMIT = 100;

    private final FootballDataClient client;
    private final FootballApiProperties props;
    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final PlayerStatRepository statRepository;
    private final TransactionTemplate tx;

    @Scheduled(initialDelay = 60_000, fixedDelay = 10_800_000)
    public void syncAll() {
        int total = 0;
        for (String code : props.competitions()) {
            try {
                ScorersResponse res = client.scorers(code, LIMIT);
                if (res == null || res.scorers() == null || res.competition() == null) continue;
                total += tx.execute(s -> save(res));
            } catch (Exception e) {
                log.error("선수 기록 동기화 실패 competition={}", code, e);
            }
        }
        log.info("선수 기록 동기화 완료: {}건", total);
    }

    private int save(ScorersResponse res) {
        League league = leagueRepository.findByExternalId(res.competition().id()).orElse(null);
        if (league == null) return 0; // 경기 동기화가 아직 리그를 안 만들었으면 다음 차례에
        Integer season = res.season() != null && res.season().startDate() != null
                ? Integer.valueOf(res.season().startDate().substring(0, 4)) : props.season();
        LocalDateTime now = LocalDateTime.now();
        int n = 0;
        for (Scorer sc : res.scorers()) {
            if (sc.player() == null || sc.player().id() == null) continue;
            Team team = sc.team() == null ? null : teamRepository.findByExternalId(sc.team().id()).orElse(null);
            Player player = playerRepository.findByExternalId(sc.player().id()).orElse(null);
            if (player == null) {
                player = Player.create(sc.player().id());
                player.update(sc.player().name(), sc.player().position(), sc.player().shirtNumber(),
                        sc.player().nationality(), Player.parseDate(sc.player().dateOfBirth()), team,
                        sc.team() == null ? null : sc.team().shortName(), sc.team() == null ? null : sc.team().crest(), now);
                player = playerRepository.save(player);
            }
            final Player p = player;
            PlayerStat stat = statRepository.findByPlayerIdAndLeagueIdAndSeason(p.getId(), league.getId(), season)
                    .orElseGet(() -> PlayerStat.create(p, league, season));
            stat.update(team, sc.playedMatches(), sc.goals(), sc.assists(), sc.penalties());
            statRepository.save(stat);
            n++;
        }
        return n;
    }
}
