package Eojinn.FC1878.transfer;

import java.time.LocalDate;
import java.time.Month;

/**
 * 이적시장 기간. 날짜를 보고 어느 이적시장인지 정함.
 * - 여름: 6~9월 → "2026-summer"
 * - 겨울: 12~2월 → "2026-winter" (12월은 다음 해 겨울로 셈)
 * - 그 밖의 달(자유계약 등): "2026-etc"
 */
public record TransferWindow(String key, String label, LocalDate start, LocalDate end) {

    public static TransferWindow of(LocalDate date) {
        int year = date.getYear();
        Month m = date.getMonth();
        if (m.getValue() >= 6 && m.getValue() <= 9) return summer(year);
        if (m == Month.DECEMBER) return winter(year + 1);
        if (m.getValue() <= 2) return winter(year);
        return new TransferWindow(year + "-etc", year + " 기타",
                LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
    }

    /** "2026-summer" 같은 키를 기간으로 바꿈. 형식이 틀리면 null */
    public static TransferWindow parse(String key) {
        if (key == null || !key.matches("\\d{4}-(summer|winter|etc)")) return null;
        int year = Integer.parseInt(key.substring(0, 4));
        return switch (key.substring(5)) {
            case "summer" -> summer(year);
            case "winter" -> winter(year);
            default -> new TransferWindow(key, year + " 기타", LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
        };
    }

    public boolean contains(LocalDate date) {
        // "기타"는 여름·겨울이 아닌 날짜만
        if (key.endsWith("-etc")) return of(date).key().equals(key);
        return !date.isBefore(start) && !date.isAfter(end);
    }

    private static TransferWindow summer(int year) {
        return new TransferWindow(year + "-summer", year + " 여름",
                LocalDate.of(year, 6, 1), LocalDate.of(year, 9, 30));
    }

    private static TransferWindow winter(int year) {
        return new TransferWindow(year + "-winter", year + " 겨울",
                LocalDate.of(year - 1, 12, 1), LocalDate.of(year, 2, 1).withDayOfMonth(LocalDate.of(year, 2, 1).lengthOfMonth()));
    }
}
