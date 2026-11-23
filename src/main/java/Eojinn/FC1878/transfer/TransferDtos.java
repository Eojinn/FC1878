package Eojinn.FC1878.transfer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class TransferDtos {

    private TransferDtos() {
    }

    /**
     * windows: 고를 수 있는 이적시장 (최신순), window: 지금 보고 있는 이적시장 ("all" 이면 전체)
     * leagues: 리그 필터에 보여줄 리그 (이적이 많은 순)
     */
    public record TransfersResponse(String window, List<WindowOption> windows, List<String> leagues,
                                    Summary summary, List<TransferResponse> items) {
    }

    public record WindowOption(String key, String label, long count) {
    }

    /** 걸러진 목록 기준 합계 (총 이적료는 공개된 기본 이적료만 더함) */
    public record Summary(long count, long koreanCount, BigDecimal totalFeeMillionEur) {
    }

    public record TransferResponse(Long id, String playerName, String playerNameEn, String nationality,
                                   boolean korean, String position, String fromClub, String fromLeague,
                                   String toClub, String toLeague, String type, BigDecimal feeMillionEur,
                                   String feeNote, LocalDate transferDate, LocalDate contractUntil) {
        public static TransferResponse from(Transfer t) {
            return new TransferResponse(t.getId(), t.getPlayerName(), t.getPlayerNameEn(), t.getNationality(),
                    t.isKorean(), t.getPosition().name(), t.getFromClub(), t.getFromLeague(),
                    t.getToClub(), t.getToLeague(), t.getType().name(), t.getFeeMillionEur(),
                    t.getFeeNote(), t.getTransferDate(), t.getContractUntil());
        }
    }
}
