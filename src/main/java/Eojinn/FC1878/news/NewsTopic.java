package Eojinn.FC1878.news;

import java.util.Arrays;

/** 뉴스 주제 → 네이버 뉴스 검색어 */
public enum NewsTopic {
    ALL("해외축구", "해외축구"),
    EPL("프리미어리그", "프리미어리그 축구"),
    LALIGA("라리가", "라리가 축구"),
    BUNDESLIGA("분데스리가", "분데스리가 축구"),
    SERIE_A("세리에A", "세리에A 축구"),
    LIGUE_1("리그1", "프랑스 리그1 축구"),
    UCL("챔피언스리그", "UEFA 챔피언스리그"),
    OVERSEAS("해외파", "해외파 축구 선수"),
    TRANSFER("이적시장", "축구 이적시장");

    private final String label;
    private final String query;

    NewsTopic(String label, String query) {
        this.label = label;
        this.query = query;
    }

    public String label() {
        return label;
    }

    public String query() {
        return query;
    }

    /** 모르는 값이면 전체(해외축구) */
    public static NewsTopic from(String key) {
        return Arrays.stream(values()).filter(t -> t.name().equalsIgnoreCase(key)).findFirst().orElse(ALL);
    }
}
