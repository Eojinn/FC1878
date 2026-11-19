package Eojinn.FC1878.prediction;

import Eojinn.FC1878.football.domain.Fixture;
import Eojinn.FC1878.football.domain.FixtureStatus;
import Eojinn.FC1878.football.repository.FixtureRepository;
import Eojinn.FC1878.global.common.TimeUtil;
import Eojinn.FC1878.global.error.BusinessException;
import Eojinn.FC1878.global.error.ErrorCode;
import Eojinn.FC1878.member.Member;
import Eojinn.FC1878.member.MemberService;
import Eojinn.FC1878.prediction.PredictionDtos.PredictionStatsResponse;
import Eojinn.FC1878.ranking.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionService {

    private final PredictionRepository predictionRepository;
    private final FixtureRepository fixtureRepository;
    private final MemberService memberService;
    private final RankingService rankingService;

    @Value("${app.prediction.win-point}")
    private int winPoint;

    /** 예측 등록/변경 (킥오프 전까지 변경 가능) */
    @Transactional
    public PredictionStatsResponse predict(Long memberId, Long fixtureId, Pick pick) {
        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FIXTURE_NOT_FOUND));
        if (!fixture.isPredictable(TimeUtil.nowKst())) throw new BusinessException(ErrorCode.PREDICTION_CLOSED);

        predictionRepository.findByMemberIdAndFixtureId(memberId, fixtureId)
                .ifPresentOrElse(p -> p.changePick(pick), () -> {
                    Member member = memberService.getMember(memberId);
                    predictionRepository.save(Prediction.of(member, fixture, pick));
                });
        predictionRepository.flush();
        return stats(fixtureId, memberId);
    }

    @Transactional(readOnly = true)
    public PredictionStatsResponse stats(Long fixtureId, Long memberIdOrNull) {
        Map<Pick, Long> counts = new EnumMap<>(Pick.class);
        for (Object[] row : predictionRepository.countByPick(fixtureId)) {
            counts.put((Pick) row[0], (Long) row[1]);
        }
        long home = counts.getOrDefault(Pick.HOME, 0L);
        long draw = counts.getOrDefault(Pick.DRAW, 0L);
        long away = counts.getOrDefault(Pick.AWAY, 0L);
        long total = home + draw + away;

        int homePct = percent(home, total);
        int drawPct = percent(draw, total);
        int awayPct = total == 0 ? 0 : 100 - homePct - drawPct; // 합계 100% 보정

        Pick myPick = memberIdOrNull == null ? null
                : predictionRepository.findByMemberIdAndFixtureId(memberIdOrNull, fixtureId)
                .map(Prediction::getPick).orElse(null);
        return new PredictionStatsResponse(fixtureId, total, homePct, drawPct, awayPct, myPick);
    }

    /** 경기 종료 시 호출: 적중자에게 포인트 지급 + 랭킹 반영 */
    @Transactional
    public void settle(Long fixtureId) {
        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FIXTURE_NOT_FOUND));
        if (fixture.isSettled() || fixture.getStatus() != FixtureStatus.FINISHED
                || fixture.getHomeScore() == null || fixture.getAwayScore() == null) {
            return;
        }
        // 승부예측은 정규시간(+연장) 스코어 기준. 승부차기는 무승부로 처리
        Pick actual = Pick.resultOf(fixture.getHomeScore(), fixture.getAwayScore());

        int winners = 0;
        for (Prediction p : predictionRepository.findByFixtureIdAndResult(fixtureId, PredictionResult.PENDING)) {
            if (p.settle(actual)) {
                p.getMember().addPoint(winPoint);
                rankingService.addScore(p.getMember().getId(), winPoint);
                winners++;
            }
        }
        fixture.markSettled();
        log.info("fixture {} settled: result={}, winners={}", fixtureId, actual, winners);
    }

    private int percent(long part, long total) {
        return total == 0 ? 0 : (int) Math.round(part * 100.0 / total);
    }
}
