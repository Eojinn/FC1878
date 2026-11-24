package Eojinn.FC1878.career;

import Eojinn.FC1878.career.CareerDtos.CareerEntry;
import Eojinn.FC1878.career.CareerDtos.CareerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 선수 커리어 (Wikidata).
 * 1) 이름으로 후보 검색 → 2) 축구 선수 + (생년월일 / 한국 국적)으로 한 명 고르기 → 3) 프로필·소속팀 이력 조회
 * 같은 선수는 하루 동안 메모리에 저장해 두고 씀 (못 찾은 경우는 1시간).
 */
@Service
@RequiredArgsConstructor
public class CareerService {

    private static final Duration TTL = Duration.ofHours(24);
    private static final Duration MISS_TTL = Duration.ofHours(1);
    private static final int MAX_CACHE = 500;

    private final WikidataClient wikidata;
    private final Map<String, Cached> cache = new ConcurrentHashMap<>();

    /**
     * @param name        선수 이름 (영문 또는 한글)
     * @param language    "en" 또는 "ko" (검색할 이름의 언어)
     * @param birthDate   알면 생년월일로 동명이인 구분 (null 가능)
     * @param preferKorea 한국 선수면 true (한국 국적 후보를 먼저 고름)
     */
    public CareerResponse lookup(String name, String language, LocalDate birthDate, boolean preferKorea) {
        if (name == null || name.isBlank()) return CareerResponse.notFound();
        String lang = "ko".equals(language) ? "ko" : "en";
        String key = (lang + "|" + name.trim() + "|" + birthDate + "|" + preferKorea).toLowerCase(Locale.ROOT);
        Cached hit = cache.get(key);
        if (hit != null && hit.expiresAt().isAfter(Instant.now())) return hit.response();

        CareerResponse res = fetch(name.trim(), lang, birthDate, preferKorea);
        if (res == null) return CareerResponse.notFound(); // Wikidata 연결 실패: 저장하지 않고 다음에 다시 시도
        if (cache.size() > MAX_CACHE) cache.clear();
        cache.put(key, new Cached(res, Instant.now().plus(res.found() ? TTL : MISS_TTL)));
        return res;
    }

    private CareerResponse fetch(String name, String lang, LocalDate birthDate, boolean preferKorea) {
        List<String> candidates = wikidata.search(name, lang);
        if (candidates.isEmpty()) return CareerResponse.notFound();

        String qid = pick(candidates, birthDate, preferKorea);
        if (qid == null) return CareerResponse.notFound();

        List<Map<String, String>> profile = wikidata.query(profileQuery(qid));
        List<Map<String, String>> nation = wikidata.query(nationalityQuery(qid));
        List<Map<String, String>> career = wikidata.query(careerQuery(qid));
        if (profile == null || career == null) return null;

        String en = first(profile, "label");
        String ko = first(profile, "ko");
        String dob = Optional.ofNullable(first(profile, "dob")).map(d -> d.substring(0, 10)).orElse(null);
        Integer height = Optional.ofNullable(first(profile, "height"))
                .map(h -> new BigDecimal(h).multiply(BigDecimal.valueOf(100)).intValue()).orElse(null);
        List<String> positions = profile.stream().map(r -> r.get("posLabel")).filter(Objects::nonNull)
                .distinct().map(CareerService::positionKo).toList();
        String nationality = nation == null || nation.isEmpty() ? null
                : Optional.ofNullable(nation.get(0).get("ko")).orElse(nation.get(0).get("en"));

        // 같은 소속 기록(statement)이 여러 줄로 올 수 있어서 하나로
        Map<String, Map<String, String>> byStatement = new LinkedHashMap<>();
        for (Map<String, String> r : career) byStatement.putIfAbsent(r.get("st"), r);

        List<CareerEntry> clubs = new ArrayList<>();
        List<CareerEntry> national = new ArrayList<>();
        for (Map<String, String> r : byStatement.values()) {
            String teamEn = r.get("en");
            String team = Optional.ofNullable(r.get("ko")).orElse(teamEn);
            if (team == null) continue;
            String end = date(r.get("end"), r.get("endPrecision"));
            CareerEntry entry = new CareerEntry(team, teamEn, date(r.get("start"), r.get("startPrecision")), end,
                    r.get("end") == null, "true".equals(r.get("loan")), integer(r.get("apps")), integer(r.get("goals")));
            boolean isNational = "true".equals(r.get("national"))
                    || (teamEn != null && teamEn.toLowerCase(Locale.ROOT).contains("national"));
            (isNational ? national : clubs).add(entry);
        }
        Comparator<CareerEntry> order = Comparator.comparing(CareerEntry::start, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(CareerEntry::end, Comparator.nullsLast(Comparator.naturalOrder()));
        clubs.sort(order);
        national.sort(order);

        Integer apps = sum(clubs, CareerEntry::apps);
        Integer goals = sum(clubs, CareerEntry::goals);
        return new CareerResponse(true, qid, "https://www.wikidata.org/wiki/" + qid, en, ko, dob, height, positions,
                nationality, clubs, national, apps, goals);
    }

    /** 후보 중 축구 선수만. 생년월일이 맞는 사람 > 한국 국적(원할 때) > 검색 순서 */
    private String pick(List<String> candidates, LocalDate birthDate, boolean preferKorea) {
        String values = candidates.stream().filter(q -> q.matches("Q\\d+")).map(q -> "wd:" + q)
                .collect(Collectors.joining(" "));
        if (values.isEmpty()) return null;
        List<Map<String, String>> rows = wikidata.query("""
                SELECT ?p ?dob ?kr WHERE {
                  VALUES ?p { %s }
                  ?p wdt:P106 wd:Q937857 .
                  OPTIONAL { ?p wdt:P569 ?dob }
                  BIND(EXISTS { ?p wdt:P27 wd:Q884 } AS ?kr)
                }
                """.formatted(values));
        if (rows == null || rows.isEmpty()) return null;
        Map<String, Map<String, String>> byId = new LinkedHashMap<>();
        for (Map<String, String> r : rows) byId.putIfAbsent(qidOf(r.get("p")), r);

        List<String> footballers = candidates.stream().filter(byId::containsKey).toList();
        if (birthDate != null) {
            for (String q : footballers) {
                String dob = byId.get(q).get("dob");
                if (dob != null && dob.startsWith(birthDate.toString())) return q;
            }
        }
        if (preferKorea) {
            for (String q : footballers) if ("true".equals(byId.get(q).get("kr"))) return q;
        }
        // 생년월일을 아는데 맞는 사람이 없으면 엉뚱한 선수를 보여 주지 않도록 포기
        return birthDate != null ? null : footballers.stream().findFirst().orElse(null);
    }

    /* ===== SPARQL ===== */

    private static String profileQuery(String q) {
        return """
                SELECT ?label ?ko ?dob ?height ?posLabel WHERE {
                  OPTIONAL { wd:%1$s rdfs:label ?label FILTER(LANG(?label) = "en") }
                  OPTIONAL { wd:%1$s rdfs:label ?ko FILTER(LANG(?ko) = "ko") }
                  OPTIONAL { wd:%1$s wdt:P569 ?dob }
                  OPTIONAL { wd:%1$s p:P2048/psn:P2048/wikibase:quantityAmount ?height }
                  OPTIONAL { wd:%1$s wdt:P413 ?pos . ?pos rdfs:label ?posLabel FILTER(LANG(?posLabel) = "en") }
                }
                """.formatted(q);
    }

    /** 국적: 대표팀 국가(P1532)를 먼저, 없으면 시민권(P27) */
    private static String nationalityQuery(String q) {
        return """
                SELECT ?ko ?en WHERE {
                  { wd:%1$s wdt:P1532 ?n . BIND(1 AS ?o) } UNION { wd:%1$s wdt:P27 ?n . BIND(2 AS ?o) }
                  OPTIONAL { ?n rdfs:label ?ko FILTER(LANG(?ko) = "ko") }
                  OPTIONAL { ?n rdfs:label ?en FILTER(LANG(?en) = "en") }
                } ORDER BY ?o LIMIT 1
                """.formatted(q);
    }

    /** 소속팀 이력(P54): 기간(P580~P582)·출전(P1350)·골(P1351)·임대 여부(P1642)·대표팀 여부 */
    private static String careerQuery(String q) {
        return """
                SELECT ?st ?en ?ko ?start ?startPrecision ?end ?endPrecision ?apps ?goals ?loan ?national WHERE {
                  wd:%1$s p:P54 ?st .
                  ?st ps:P54 ?team .
                  FILTER NOT EXISTS { ?st wikibase:rank wikibase:DeprecatedRank }
                  OPTIONAL { ?st pqv:P580 ?sv . ?sv wikibase:timeValue ?start ; wikibase:timePrecision ?startPrecision }
                  OPTIONAL { ?st pqv:P582 ?ev . ?ev wikibase:timeValue ?end ; wikibase:timePrecision ?endPrecision }
                  OPTIONAL { ?st pq:P1350 ?apps }
                  OPTIONAL { ?st pq:P1351 ?goals }
                  OPTIONAL { ?team rdfs:label ?en FILTER(LANG(?en) = "en") }
                  OPTIONAL { ?team rdfs:label ?ko FILTER(LANG(?ko) = "ko") }
                  BIND(EXISTS { ?st pq:P1642 ?acq . ?acq rdfs:label ?acqLabel .
                                FILTER(LANG(?acqLabel) = "en" && CONTAINS(LCASE(?acqLabel), "loan")) } AS ?loan)
                  BIND(EXISTS { ?team wdt:P31/wdt:P279* wd:Q6979593 } AS ?national)
                }
                """.formatted(q);
    }

    /* ===== 변환 ===== */

    /** "2015-08-28T00:00:00Z" + 정밀도(9=연, 10=월, 11=일) → "2015" 또는 "2015-08" */
    static String date(String value, String precision) {
        if (value == null || value.length() < 7) return null;
        String v = value.startsWith("+") ? value.substring(1) : value;
        int p = precision == null ? 11 : Integer.parseInt(precision);
        return p <= 9 ? v.substring(0, 4) : v.substring(0, 7);
    }

    private static Integer integer(String v) {
        try {
            return v == null ? null : new BigDecimal(v).intValue();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer sum(List<CareerEntry> list, java.util.function.Function<CareerEntry, Integer> f) {
        List<Integer> v = list.stream().map(f).filter(Objects::nonNull).toList();
        return v.isEmpty() ? null : v.stream().mapToInt(Integer::intValue).sum();
    }

    private static String first(List<Map<String, String>> rows, String key) {
        return rows.stream().map(r -> r.get(key)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    private static String qidOf(String uri) {
        return uri == null ? null : uri.substring(uri.lastIndexOf('/') + 1);
    }

    private static final Map<String, String> POSITIONS = Map.ofEntries(
            Map.entry("goalkeeper", "골키퍼"), Map.entry("defender", "수비수"), Map.entry("centre-back", "센터백"),
            Map.entry("center back", "센터백"), Map.entry("full-back", "풀백"), Map.entry("left-back", "레프트백"),
            Map.entry("right-back", "라이트백"), Map.entry("wing-back", "윙백"), Map.entry("midfielder", "미드필더"),
            Map.entry("defensive midfielder", "수비형 미드필더"), Map.entry("central midfielder", "중앙 미드필더"),
            Map.entry("attacking midfielder", "공격형 미드필더"), Map.entry("winger", "윙어"),
            Map.entry("forward", "공격수"), Map.entry("centre-forward", "센터 포워드"), Map.entry("striker", "스트라이커"),
            Map.entry("second striker", "세컨드 스트라이커"));

    static String positionKo(String en) {
        String k = en.toLowerCase(Locale.ROOT).replace("association football ", "").trim();
        return POSITIONS.getOrDefault(k, en);
    }

    private record Cached(CareerResponse response, Instant expiresAt) {
    }
}
