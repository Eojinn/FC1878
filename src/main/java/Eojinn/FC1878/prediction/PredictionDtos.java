package Eojinn.FC1878.prediction;

import jakarta.validation.constraints.NotNull;

public final class PredictionDtos {
    private PredictionDtos() {
    }

    public record PredictRequest(@NotNull Pick pick) {
    }

    /** 메인의 "홈 88% / 무 6% / 원정 6%" 카드용 */
    public record PredictionStatsResponse(Long fixtureId, long total,
                                          int homePercent, int drawPercent, int awayPercent,
                                          Pick myPick) {
    }
}
