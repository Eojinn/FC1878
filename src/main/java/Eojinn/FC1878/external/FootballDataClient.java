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

    /** 무료 플랜 분당 10회 → 모든 요청 사이를 최소 6.5초 띄움 (여러 동기화가 동시에 돌아도 안전) */
    private static final long MIN_GAP_MS = 6_500;

    private final RestClient restClient;
    private final FootballApiProperties props;
    private long lastRequestAt = 0;

    public FootballDataClient(FootballApiProperties props) {
        this.props = props;
        this.restClient = RestClient.builder()
                .baseUrl(props.baseUrl())
                .defaultHeader("X-Auth-Token", props.key() == null ? "" : props.key())
                .build();
    }

    /** 직전 요청에서 6.5초가 안 지났으면 기다림 */
    private synchronized void throttle() {
        long wait = lastRequestAt + MIN_GAP_MS - System.currentTimeMillis();
        if (wait > 0) {
            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        lastRequestAt = System.currentTimeMillis();
    }

    /** 설정한 대회들의 기간 내 경기 (요청 1회) */
    public List<Match> matches(LocalDate from, LocalDate to) {
        throttle();
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
        throttle();
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

    /** 대회 참가 팀 목록 (요청 1회). 실패하면 null */
    public CompetitionTeamsResponse competitionTeams(String competitionCode) {
        throttle();
        try {
            return restClient.get()
                    .uri("/competitions/{code}/teams", competitionCode)
                    .retrieve()
                    .body(CompetitionTeamsResponse.class);
        } catch (RestClientResponseException e) {
            logError("competition teams " + competitionCode, e);
            return null;
        }
    }

    /** 팀 스쿼드 (요청 1회). 실패하면 null */
    public TeamDetail team(int externalTeamId) {
        throttle();
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

    /** 대회 득점 순위 (요청 1회). limit 명까지. 실패하면 null */
    public ScorersResponse scorers(String competitionCode, int limit) {
        throttle();
        try {
            return restClient.get()
                    .uri(u -> u.path("/competitions/{code}/scorers").queryParam("limit", limit).build(competitionCode))
                    .retrieve()
                    .body(ScorersResponse.class);
        } catch (RestClientResponseException e) {
            logError("scorers " + competitionCode, e);
            return null;
        }
    }

    /** 403(플랜에 없는 대회), 429(분당 요청 초과), 400(잘못된 키) 등은 응답 본문에 이유가 담겨 옴 */
    private void logError(String what, RestClientResponseException e) {
        log.warn("football-data.org 오류 ({}): HTTP {} {}", what, e.getStatusCode().value(),
                e.getResponseBodyAsString());
    }
}
