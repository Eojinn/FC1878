package Eojinn.FC1878.external;

import Eojinn.FC1878.football.domain.Fixture;
import Eojinn.FC1878.football.domain.FixtureStatus;
import Eojinn.FC1878.football.repository.FixtureRepository;
import Eojinn.FC1878.global.common.TimeUtil;
import Eojinn.FC1878.prediction.PredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * football.api.enabled=true 일 때만 동작.
 * API-Football 무료 플랜은 하루 100회 제한이므로 주기를 보수적으로 잡음. 유료 플랜이면 라이브 주기를 1분으로 줄이면 됨.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "football.api", name = "enabled", havingValue = "true")
public class FootballSyncScheduler {

    private final FootballSyncService syncService;
    private final PredictionService predictionService;
    private final FixtureRepository fixtureRepository;
    private final FootballApiProperties props;

    /** 10분마다 오늘 + 내일 경기 동기화, 종료된 경기 정산 */
    @Scheduled(initialDelay = 5_000, fixedDelay = 600_000)
    public void syncTodayFixtures() {
        LocalDate today = LocalDate.now(TimeUtil.KST);
        try {
            syncService.syncFixtures(today.minusDays(1)); // 새벽에 끝난 유럽 경기 반영
            syncService.syncFixtures(today);
            syncService.syncFixtures(today.plusDays(1));
        } catch (Exception e) {
            log.error("경기 동기화 실패", e);
        }
        settleFinished();
    }

    /** 매일 05:00(KST) 순위표 동기화 */
    @Scheduled(cron = "0 0 5 * * *", zone = "Asia/Seoul")
    public void syncStandings() {
        for (Integer leagueId : props.leagueIds()) {
            try {
                syncService.syncStandings(leagueId, props.season());
            } catch (Exception e) {
                log.error("순위 동기화 실패 league={}", leagueId, e);
            }
        }
    }

    private void settleFinished() {
        for (Fixture f : fixtureRepository.findByStatusAndSettledFalse(FixtureStatus.FINISHED)) {
            try {
                predictionService.settle(f.getId());
            } catch (Exception e) {
                log.error("정산 실패 fixture={}", f.getId(), e);
            }
        }
    }
}
