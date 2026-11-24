package Eojinn.FC1878.player;

import Eojinn.FC1878.career.WikidataClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 외국 선수 한글 이름 채우기 (한글로 검색할 수 있게).
 * 서버 시작 10분 뒤, 이후 6시간마다 한글 이름이 없는 선수 300명씩을 Wikidata 에서 찾아봄.
 * 영문 이름이 같은 축구 선수가 여럿이면 생년월일로 구분하고, 구분이 안 되면 넣지 않음.
 * 못 찾은 선수는 30일 뒤에 다시 찾아봄.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KoreanNameSyncService {

    private static final int PER_RUN = 300;
    private static final int BATCH = 40;

    private final PlayerRepository playerRepository;
    private final WikidataClient wikidata;
    private final TransactionTemplate tx;

    @Scheduled(initialDelay = 600_000, fixedDelay = 21_600_000)
    public void fillKoreanNames() {
        LocalDateTime now = LocalDateTime.now();
        List<Player> targets = playerRepository.findNeedingKoreanName(now.minusDays(30), PageRequest.of(0, PER_RUN));
        if (targets.isEmpty()) return;
        int found = 0;
        for (int i = 0; i < targets.size(); i += BATCH) {
            List<Player> batch = targets.subList(i, Math.min(i + BATCH, targets.size()));
            Map<String, List<Map<String, String>>> byName = lookup(batch);
            if (byName == null) return; // Wikidata 연결 실패 → 다음 차례에
            found += tx.execute(s -> save(batch, byName, now));
        }
        log.info("선수 한글 이름 찾기: {}명 중 {}명 찾음", targets.size(), found);
    }

    /** 영문 이름 → [{ko, dob}] (같은 이름의 축구 선수가 여럿일 수 있음) */
    private Map<String, List<Map<String, String>>> lookup(List<Player> batch) {
        String values = batch.stream().map(Player::getName).distinct()
                .map(n -> "\"" + n.replace("\\", "\\\\").replace("\"", "\\\"") + "\"@en")
                .collect(Collectors.joining(" "));
        List<Map<String, String>> rows = wikidata.query("""
                SELECT ?name ?ko ?dob WHERE {
                  VALUES ?name { %s }
                  { ?p rdfs:label ?name } UNION { ?p skos:altLabel ?name }
                  ?p wdt:P106 wd:Q937857 ; rdfs:label ?ko .
                  FILTER(LANG(?ko) = "ko")
                  OPTIONAL { ?p wdt:P569 ?dob }
                }
                """.formatted(values));
        if (rows == null) return null;
        return rows.stream().filter(r -> r.get("name") != null)
                .collect(Collectors.groupingBy(r -> r.get("name").toLowerCase(Locale.ROOT)));
    }

    private int save(List<Player> batch, Map<String, List<Map<String, String>>> byName, LocalDateTime now) {
        int n = 0;
        for (Player detached : batch) {
            Player p = playerRepository.findById(detached.getId()).orElse(null);
            if (p == null) continue;
            String ko = pick(byName.getOrDefault(p.getName().toLowerCase(Locale.ROOT), List.of()), p);
            p.markKoreanName(ko, now);
            if (ko != null) n++;
        }
        return n;
    }

    /** 후보가 한 명이면 그 사람, 여럿이면 생년월일이 맞는 사람만 */
    private static String pick(List<Map<String, String>> candidates, Player p) {
        Set<String> names = candidates.stream().map(r -> r.get("ko")).filter(Objects::nonNull).collect(Collectors.toSet());
        if (names.size() == 1 && (p.getDateOfBirth() == null || candidates.stream()
                .allMatch(r -> r.get("dob") == null || r.get("dob").startsWith(p.getDateOfBirth().toString())))) {
            return names.iterator().next();
        }
        if (p.getDateOfBirth() == null) return null;
        return candidates.stream()
                .filter(r -> r.get("dob") != null && r.get("dob").startsWith(p.getDateOfBirth().toString()))
                .map(r -> r.get("ko")).findFirst().orElse(null);
    }
}
