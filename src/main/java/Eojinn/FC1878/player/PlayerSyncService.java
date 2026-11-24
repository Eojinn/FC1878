package Eojinn.FC1878.player;

import Eojinn.FC1878.external.FootballApiProperties;
import Eojinn.FC1878.external.FootballDataClient;
import Eojinn.FC1878.external.FootballDataDtos.CompetitionTeam;
import Eojinn.FC1878.external.FootballDataDtos.CompetitionTeamsResponse;
import Eojinn.FC1878.external.FootballDataDtos.SquadPlayer;
import Eojinn.FC1878.external.FootballDataDtos.TeamDetail;
import Eojinn.FC1878.football.domain.Team;
import Eojinn.FC1878.football.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 선수 목록 동기화: 서버 시작 3분 뒤 1회, 이후 하루마다.
 * 대회별 팀 목록(요청 1회)에 스쿼드가 들어 있으면 그대로 쓰고, 없으면 팀마다 따로 요청.
 * 분당 10회 제한 때문에 요청 사이에 8초씩 쉬고, 다른 동기화를 막지 않도록 스케줄러 스레드를 2개로 둠.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnExpression("!'${football.api.key:}'.isBlank() and ${football.api.enabled:true}")
public class PlayerSyncService {

    private static final long GAP_MS = 1_000; // 요청 간격은 FootballDataClient 가 6.5초씩 지켜 줌

    private final FootballDataClient client;
    private final FootballApiProperties props;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final TransactionTemplate tx;

    @Scheduled(initialDelay = 180_000, fixedDelay = 86_400_000)
    public void syncAll() {
        LocalDateTime startedAt = LocalDateTime.now();
        Set<Integer> doneTeams = new HashSet<>();
        boolean allOk = true;
        int saved = 0;
        try {
            for (String code : props.competitions()) {
                CompetitionTeamsResponse res = client.competitionTeams(code);
                pause();
                if (res == null || res.teams() == null) {
                    allOk = false;
                    continue;
                }
                for (CompetitionTeam t : res.teams()) {
                    if (t.id() == null || !doneTeams.add(t.id())) continue; // 챔스·리그 중복 팀은 한 번만
                    List<SquadPlayer> squad = t.squad();
                    if (squad == null || squad.isEmpty()) {
                        TeamDetail detail = client.team(t.id());
                        pause();
                        if (detail == null) {
                            allOk = false;
                            continue;
                        }
                        squad = detail.squad();
                    }
                    if (squad != null) saved += save(t, squad, startedAt);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        // 전부 성공했을 때만, 이번에 안 보인 선수(이적·방출)를 지움
        int removed = allOk ? tx.execute(s -> playerRepository.deleteNotSyncedSince(startedAt)) : 0;
        log.info("선수 동기화 완료: 팀 {}개, 선수 {}명 반영, {}명 삭제", doneTeams.size(), saved, removed);
    }

    private int save(CompetitionTeam t, List<SquadPlayer> squad, LocalDateTime now) {
        return tx.execute(status -> {
            Team team = teamRepository.findByExternalId(t.id()).orElse(null);
            int n = 0;
            for (SquadPlayer sp : squad) {
                if (sp.id() == null || sp.name() == null) continue;
                Player p = playerRepository.findByExternalId(sp.id()).orElseGet(() -> Player.create(sp.id()));
                p.update(sp.name(), sp.position(), sp.shirtNumber(), sp.nationality(), Player.parseDate(sp.dateOfBirth()),
                        team, t.shortName() != null ? t.shortName() : t.name(), t.crest(), now);
                playerRepository.save(p);
                n++;
            }
            return n;
        });
    }

    private static void pause() throws InterruptedException {
        Thread.sleep(GAP_MS);
    }
}
