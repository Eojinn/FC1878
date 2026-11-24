package Eojinn.FC1878.external;

import Eojinn.FC1878.external.FootballDataDtos.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDate;
import java.util.List;

/**
 * football-data.org v4 클라이언트.
 * 무료 플랜: 주요 12개 대회 현재 시즌, 분당 10회 요청, 스코어는 실시간이 아니라 지연 제공.
 */
@Slf4j
@Component
public class FootballDataClient {

    private final RestClient restClient;
    private final FootballApiProperties props;

    public FootballDataClient(FootballApiProperties props) {
        this.props = props;
        this.restClient = RestClient.builder()
                .baseUrl(props.baseUrl())
                .defaultHeader("X-Auth-Token", props.key() == null ? "" : props.key())
                .build();
    }

    /** 설정한 대회들의 기간 내 경기 (요청 1회) */
    public List<Match> matches(LocalDate from, LocalDate to) {
        try {
            MatchesResponse res = restClient.get()
                    .uri(u -> u.path("/matches")
                            .queryParam("dateFrom", from.toString())
                            .queryParam("dateTo", to.toString())
                            .queryParam("competitions", String.join(",", props.competitions()))
                            .build())
                    .retrieve()
                    .body(MatchesResponse.class);
            return res == null || res.matches() == null ? List.of() : res.matches();
        } catch (RestClientResponseException e) {
            logError("matches " + from + "~" + to, e);
            return List.of();
        }
    }

    /** 대회 순위표. 실패하면 null */
    public StandingsResponse standings(String competitionCode) {
        try {
            return restClient.get()
                    .uri("/competitions/{code}/standings", competitionCode)
                    .retrieve()
                    .body(StandingsResponse.class);
        } catch (RestClientResponseException e) {
            logError("standings " + competitionCode, e);
            return null;
        }
    }

    /** 팀 스쿼드 (요청 1회). 실패하면 null */
    public TeamDetail team(int externalTeamId) {
        try {
            return restClient.get()
                    .uri("/teams/{id}", externalTeamId)
                    .retrieve()
                    .body(TeamDetail.class);
        } catch (RestClientResponseException e) {
            logError("team " + externalTeamId, e);
            return null;
        }
    }

    /** 403(플랜에 없는 대회), 429(분당 요청 초과), 400(잘못된 키) 등은 응답 본문에 이유가 담겨 옴 */
    private void logError(String what, RestClientResponseException e) {
        log.warn("football-data.org 오류 ({}): HTTP {} {}", what, e.getStatusCode().value(),
                e.getResponseBodyAsString());
    }
}
