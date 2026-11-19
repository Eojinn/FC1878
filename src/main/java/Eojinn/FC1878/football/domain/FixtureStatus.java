package Eojinn.FC1878.football.domain;

public enum FixtureStatus {
    SCHEDULED, LIVE, FINISHED, POSTPONED, CANCELLED;

    /** API-Football status.short 코드 → 내부 상태 */
    public static FixtureStatus fromApiCode(String code) {
        if (code == null) return SCHEDULED;
        return switch (code) {
            case "1H", "HT", "2H", "ET", "BT", "P", "LIVE", "INT" -> LIVE;
            case "FT", "AET", "PEN" -> FINISHED;
            case "PST", "SUSP" -> POSTPONED;
            case "CANC", "ABD", "AWD", "WO" -> CANCELLED;
            default -> SCHEDULED; // TBD, NS
        };
    }
}
