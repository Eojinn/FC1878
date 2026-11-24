package Eojinn.FC1878.career;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.*;

/**
 * Wikidata (누구나 쓸 수 있는 공개 데이터, CC0) 조회.
 * - 이름 검색: https://www.wikidata.org/w/api.php?action=wbsearchentities
 * - 상세 질의: https://query.wikidata.org/sparql (SPARQL)
 * Wikidata 는 요청에 프로그램 이름(User-Agent)을 적도록 요구함.
 */
@Slf4j
@Component
public class WikidataClient {

    private static final String USER_AGENT = "FC1878/1.0 (https://github.com/Eojinn/FC1878; football community side project)";

    private final RestClient api;
    private final RestClient sparql;

    public WikidataClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.api = RestClient.builder().requestFactory(factory)
                .baseUrl("https://www.wikidata.org").defaultHeader("User-Agent", USER_AGENT).build();
        this.sparql = RestClient.builder().requestFactory(factory)
                .baseUrl("https://query.wikidata.org").defaultHeader("User-Agent", USER_AGENT).build();
    }

    /** 이름으로 항목 검색 → Q 번호 목록 (검색 순서 유지). 실패하면 빈 목록 */
    public List<String> search(String name, String language) {
        try {
            SearchResponse res = api.get()
                    .uri(u -> u.path("/w/api.php")
                            .queryParam("action", "wbsearchentities")
                            .queryParam("search", name)
                            .queryParam("language", language)
                            .queryParam("uselang", language)
                            .queryParam("type", "item")
                            .queryParam("limit", 10)
                            .queryParam("format", "json")
                            .build())
                    .retrieve()
                    .body(SearchResponse.class);
            if (res == null || res.search() == null) return List.of();
            return res.search().stream().map(SearchItem::id).filter(Objects::nonNull).toList();
        } catch (RestClientException e) {
            log.warn("Wikidata 검색 실패 name={}: {}", name, e.getMessage());
            return List.of();
        }
    }

    /** SPARQL 결과를 [{변수: 값}] 으로. 실패하면 null */
    public List<Map<String, String>> query(String sparqlQuery) {
        try {
            SparqlResponse res = sparql.get()
                    .uri(u -> u.path("/sparql").queryParam("query", "{q}").queryParam("format", "json").build(sparqlQuery))
                    .accept(MediaType.parseMediaType("application/sparql-results+json"))
                    .retrieve()
                    .body(SparqlResponse.class);
            if (res == null || res.results() == null || res.results().bindings() == null) return List.of();
            List<Map<String, String>> rows = new ArrayList<>();
            for (Map<String, Binding> b : res.results().bindings()) {
                Map<String, String> row = new HashMap<>();
                b.forEach((k, v) -> row.put(k, v == null ? null : v.value()));
                rows.add(row);
            }
            return rows;
        } catch (RestClientException e) {
            log.warn("Wikidata 질의 실패: {}", e.getMessage());
            return null;
        }
    }

    record SearchResponse(List<SearchItem> search) {
    }

    record SearchItem(String id, String label, String description) {
    }

    record SparqlResponse(Results results) {
    }

    record Results(List<Map<String, Binding>> bindings) {
    }

    record Binding(String type, String value) {
    }
}
