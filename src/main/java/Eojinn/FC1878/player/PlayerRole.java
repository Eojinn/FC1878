package Eojinn.FC1878.player;

import java.util.Locale;

/** football-data 포지션("Centre-Back", "Offence" …) → GK / DF / MF / FW */
public final class PlayerRole {

    private PlayerRole() {
    }

    public static String of(String position) {
        if (position == null) return "MF";
        String s = position.toLowerCase(Locale.ROOT);
        if (s.contains("goalkeeper")) return "GK";
        if (s.contains("back") || s.contains("defen")) return "DF";
        if (s.contains("forward") || s.contains("offence") || s.contains("winger") || s.contains("striker")) return "FW";
        return "MF";
    }

    public static int order(String role) {
        return switch (role) {
            case "GK" -> 0;
            case "DF" -> 1;
            case "MF" -> 2;
            default -> 3;
        };
    }
}
