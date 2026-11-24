package Eojinn.FC1878.news;

import Eojinn.FC1878.news.NewsDtos.NaverNewsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * 네이버 API 허브 뉴스 검색 (GET /search/v1/news). 하루 25,000회 제한.
 * 인증 헤더: X-NCP-APIGW-API-KEY-ID (Client ID), X-NCP-APIGW-API-KEY (Client Secret)
 */
@Slf4j
@Component
public class NaverNewsClient {

    private final RestClient restClient;

    public NaverNewsClient(NewsProperties props) {
        this.restClient = RestClient.builder()
                .baseUrl(props.naverBaseUrl())
                .defaultHeader("X-NCP-APIGW-API-KEY-ID", props.naverClientId() == null ? "" : props.naverClientId())
                .defaultHeader("X-NCP-APIGW-API-KEY", props.naverClientSecret() == null ? "" : props.naverClientSecret())
                .build();
    }

    /** 최신순 검색. 실패하면 null */
    public NaverNewsResponse search(String query, int start, int display) {
        try {
            return restClient.get()
                    .uri(u -> u.path("/search/v1/news")
                            .queryParam("query", query)
                            .queryParam("start", start)
                            .queryParam("display", display)
                            .queryParam("sort", "date")
                            .build())
                    .retrieve()
                    .body(NaverNewsResponse.class);
        } catch (RestClientResponseException e) {
            // 401: 키가 틀림, 429: 호출 한도 초과, 400 SE01~SE06: 요청 값 문제
            log.warn("네이버 뉴스 검색 오류: HTTP {} {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            return null;
        } catch (RestClientException e) {
            log.warn("네이버 뉴스 검색 연결 실패: {}", e.getMessage());
            return null;
        }
    }
}
