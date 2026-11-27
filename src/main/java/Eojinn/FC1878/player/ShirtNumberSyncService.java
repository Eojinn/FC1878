package Eojinn.FC1878.player;

import Eojinn.FC1878.career.WikidataClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 선수 등번호 채우기.
 * football-data.org 무료 데이터에는 등번호가 거의 없어서, Wikidata 의 "소속팀(P54) + 등번호(P1618)" 기록에서 찾아 넣음.
 * - 끝난 날짜가 없는(지금 뛰는) 소속팀 기록만 보고, 국가대표 번호는 뺌
 * - 같은 이름이 여럿이면 생년월일로, 소속팀이 여럿이면(임대 등) 우리 DB 의 소속팀 이름과 비슷한 쪽으로 고름
 * - 서버 시작 12분 뒤, 이후 6시간마다 등번호 없는 선수 600명씩. 못 찾은 선수는 7일 뒤 다시 찾아봄
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShirtNumberSyncService {

    private static final int PER_RUN = 600;
    private static final int BATCH = 40;

    private final PlayerRepository playerRepository;
    private final WikidataClient wikidata;
    private final TransactionTemplate tx;

    @Scheduled(initialDelay = 720_000, fixedDelay = 21_600_000)
    public void fillShirtNumbers() {
        LocalDateTime now = LocalDateTime.now();
        List<Player> targets = playerRepository.findNeedingShirtNumber(now.minusDays(7), PageRequest.of(0, PER_RUN));
        if (targets.isEmpty()) return;
        int found = 0;
        for (int i = 0; i < targets.size(); i += BATCH) {
            List<Player> batch = targets.subList(i, Math.min(i + BATCH, targets.size()));
            Map<String, List<Map<String, String>>> byName = lookup(batch);
            if (byName == null) break; // Wikidata 연결 실패 → 다음 차례에
            found += tx.execute(s -> save(batch, byName, now));
        }
        log.info("선수 등번호 찾기: {}명 중 {}명 찾음", targets.size(), found);
    }

    /** 영문 이름 → [{num, team, dob}] (지금 소속팀의 등번호들) */
    private Map<String, List<Map<String, String>>> lookup(List<Player> batch) {
        String values = batch.stream().map(Player::getName).filter(Objects::nonNull).distinct()
                .map(n -> "\"" + n.replace("\\", "\\\\").replace("\"", "\\\"") + "\"@en")
                .collect(Collectors.joining(" "));
        if (values.isEmpty()) return Map.of();
        List<Map<String, String>> rows = wikidata.query("""
                SELECT ?name ?num ?team ?dob WHERE {
                  VALUES ?name { %s }
                  { ?p rdfs:label ?name } UNION { ?p skos:altLabel ?name }
                  ?p wdt:P106 wd:Q937857 ;
                     p:P54 ?st .
                  ?st ps:P54 ?club ;
                      pq:P1618 ?num .
                  FILTER NOT EXISTS { ?st pq:P582 ?end }
                  FILTER NOT EXISTS { ?club wdt:P31 wd:Q6979593 }
                  ?club rdfs:label ?team . FILTER(LANG(?team) = "en")
                  OPTIONAL { ?p wdt:P569 ?dob }
                }
                """.formatted(values));
        if (rows == null) return null;
        return rows.stream().filter(r -> r.get("name") != null && r.get("num") != null)
                .collect(Collectors.groupingBy(r -> r.get("name").toLowerCase(Locale.ROOT)));
    }

    private int save(List<Player> batch, Map<String, List<Map<String, String>>> byName, LocalDateTime now) {
        int n = 0;
        for (Player detached : batch) {
            Player p = playerRepository.findById(detached.getId()).orElse(null);
            if (p == null || p.getShirtNumber() != null) continue;
            Integer num = pick(byName.getOrDefault(p.getName().toLowerCase(Locale.ROOT), List.of()), p);
            p.markShirtNumber(num, now);
            if (num != null) n++;
        }
        return n;
    }

    /** 생년월일이 맞는 후보 → 소속팀 이름이 비슷한 번호 → 번호가 하나뿐이면 그 번호 */
    static Integer pick(List<Map<String, String>> candidates, Player p) {
        List<Map<String, String>> list = candidates;
        if (p.getDateOfBirth() != null) {
            String dob = p.getDateOfBirth().toString();
            List<Map<String, String>> sameDob = list.stream().filter(r -> r.get("dob") != null && r.get("dob").startsWith(dob)).toList();
            boolean anyDob = list.stream().anyMatch(r -> r.get("dob") != null);
            if (!sameDob.isEmpty()) list = sameDob;
            else if (anyDob) return null; // 생년월일이 다른 동명이인뿐
        }
        if (list.isEmpty()) return null;
        if (p.getTeamName() != null) {
            Set<Integer> teamNums = list.stream().filter(r -> sameTeam(r.get("team"), p.getTeamName()))
                    .map(r -> toNumber(r.get("num"))).filter(Objects::nonNull).collect(Collectors.toSet());
            if (teamNums.size() == 1) return teamNums.iterator().next();
        }
        Set<Integer> nums = list.stream().map(r -> toNumber(r.get("num"))).filter(Objects::nonNull).collect(Collectors.toSet());
        return nums.size() == 1 ? nums.iterator().next() : null;
    }

    private static Integer toNumber(String s) {
        try {
            int n = Integer.parseInt(s.trim());
            return n > 0 && n < 100 ? n : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * "Man United" ↔ "Manchester United F.C." 처럼 같은 팀인지 비교.
     * 짧은 쪽 이름의 단어가 모두 상대에 있어야 같은 팀 (단어가 같거나, 한쪽이 다른 쪽의 앞부분이거나, 앞 4글자가 같으면)
     * → "Man City" 는 "Manchester United" 와 다른 팀으로 봄
     */
    static boolean sameTeam(String a, String b) {
        Set<String> ta = words(a);
        Set<String> tb = words(b);
        if (ta.isEmpty() || tb.isEmpty()) return false;
        Set<String> small = ta.size() <= tb.size() ? ta : tb;
        Set<String> big = small == ta ? tb : ta;
        return small.stream().allMatch(x -> big.stream().anyMatch(y -> similarWord(x, y)));
    }

    private static boolean similarWord(String x, String y) {
        if (x.equals(y) || x.startsWith(y) || y.startsWith(x)) return true;
        return x.length() >= 4 && y.length() >= 4 && x.substring(0, 4).equals(y.substring(0, 4));
    }

    private static final Set<String> NOISE = Set.of("fc", "cf", "afc", "ac", "sc", "ssc", "as", "club", "de", "the", "football");

    private static Set<String> words(String s) {
        if (s == null) return Set.of();
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT)
                .replace("ß", "ss").replaceAll("[^a-z0-9]+", " ").trim();
        return Arrays.stream(n.split(" ")).filter(w -> w.length() >= 2 && !NOISE.contains(w)).collect(Collectors.toSet());
    }
}
