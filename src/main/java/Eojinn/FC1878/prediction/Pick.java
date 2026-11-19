package Eojinn.FC1878.prediction;

public enum Pick {
    HOME, DRAW, AWAY;

    public static Pick resultOf(int home, int away) {
        if (home > away) return HOME;
        if (home < away) return AWAY;
        return DRAW;
    }
}
