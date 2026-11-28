package Eojinn.FC1878.external;

import Eojinn.FC1878.football.domain.Fixture;
import Eojinn.FC1878.football.domain.FixtureStatus;
import Eojinn.FC1878.football.repository.FixtureRepository;
import Eojinn.FC1878.global.common.TimeUtil;
import Eojinn.FC1878.prediction.PredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * football-data.org 토큰(football.api.key)이 있을 때만 동작. football.api.enabled=false 로 끌 수 있다.
 * football-data.org 무료 플랜은 분당 10회 제한 → 경기는 1회 요청으로 8일치를 가져오고, 순위는 요청 사이 간격을 둔다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnExpression("!'${football.api.key:}'.isBlank() and ${football.api.enabled:true}")
public class FootballSyncScheduler {

    /** 분당 10회 제한을 넘지 않도록 순위 요청 사이에 쉬는 시간 */
    private static final long STANDINGS_GAP_MS = 500; // 실제 간격은 FootballDataClient 가 6.5초씩 지켜 줌

    private final FootballSyncService syncService;
    private final FootballDataClient client;
    private final PredictionService predictionService;
    private final FixtureRepository fixtureRepository;
    private final FootballApiProperties props;

    /** 어제 ~ 6일 뒤 경기 동기화 + 종료 경기 정산. 기본 2분 주기 (요청 1회) */
    @Scheduled(initialDelay = 5_000, fixedDelayString = "${football.api.fixture-sync-ms:120000}")
    public void syncFixtures() {
        LocalDate today = LocalDate.now(TimeUtil.KST);
        try {
            int n = syncService.syncFixtures(today.minusDays(1), today.plusDays(6));
            log.info("경기 {}건 DB 반영", n);
        } catch (Exception e) {
            log.error("경기 동기화 실패", e);
        }
        settleFinished();
    }

    /**
     * 서버 시작 20초 뒤 1회, 이후 3시간마다 순위표 + 최근 5경기 동기화
     * (대회마다 순위표 1회 + 끝난 경기 1회 요청 → 6개 대회면 12회, 요청 간격은 FootballDataClient 가 지켜 줌)
     */
    @Scheduled(initialDelay = 20_000, fixedDelayString = "${football.api.standings-sync-ms:10800000}")
    public void syncStandings() {
        java.util.Map<String, java.util.List<FootballDataDtos.Match>> finished = new java.util.LinkedHashMap<>();
        for (String code : props.competitions()) {
            try {
                syncService.syncStandings(code);
                finished.put(code, client.finishedMatches(code));
                Thread.sleep(STANDINGS_GAP_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.error("순위 동기화 실패 competition={}", code, e);
            }
        }
        log.info("순위표 동기화 완료: {}", props.competitions());
        try {
            int n = syncService.syncForms(finished);
            log.info("최근 5경기 계산 완료: 팀 {}개", n);
        } catch (Exception e) {
            log.error("최근 5경기 계산 실패", e);
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
