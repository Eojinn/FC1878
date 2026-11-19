package Eojinn.FC1878.global.common;

import java.time.LocalDateTime;
import java.time.ZoneId;

public final class TimeUtil {
    public static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private TimeUtil() {
    }

    public static LocalDateTime nowKst() {
        return LocalDateTime.now(KST);
    }
}
