package Eojinn.FC1878.search;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 이름 검색어 정리: 대소문자·하이픈·띄어쓰기를 무시하고, 단어 순서가 달라도 찾을 수 있게.
 * 예) "minjae kim", "Kim Minjae", "kimminjae" → 모두 "Kim Min-jae" 와 일치
 */
public record NameQuery(String compact, List<String> tokens) {

    public static NameQuery of(String q) {
        String lower = q.toLowerCase(Locale.ROOT).replace("-", "").replace("·", " ").trim();
        List<String> tokens = Arrays.stream(lower.split("\\s+")).filter(t -> !t.isEmpty()).limit(3).toList();
        return new NameQuery(lower.replaceAll("\\s+", ""), tokens);
    }

    public String token(int i) {
        return i < tokens.size() ? tokens.get(i) : null;
    }

    /** 이름 후보들 중 하나라도 맞으면 true */
    public boolean matches(String... names) {
        return Stream.of(names).filter(Objects::nonNull)
                .flatMap(n -> Arrays.stream(n.split(",")))
                .anyMatch(this::matchesOne);
    }

    private boolean matchesOne(String name) {
        String lower = name.toLowerCase(Locale.ROOT).replace("-", "");
        if (lower.replaceAll("\\s+", "").contains(compact)) return true;
        return !tokens.isEmpty() && tokens.stream().allMatch(lower::contains);
    }

    /** 중복 제거용 키 (공백·하이픈·대소문자 무시) */
    public static String key(String name) {
        return name == null ? "" : name.toLowerCase(Locale.ROOT).replace("-", "").replaceAll("\\s+", "");
    }
}
