package Eojinn.FC1878.external;

import Eojinn.FC1878.external.ApiFootballDtos.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

@Component
public class ApiFootballClient {

    private final RestClient restClient;

    public ApiFootballClient(FootballApiProperties props) {
        this.restClient = RestClient.builder()
                .baseUrl(props.baseUrl())
                .defaultHeader("x-apisports-key", props.key() == null ? "" : props.key())
                .build();
    }

    public List<FixtureItem> fixturesByDate(LocalDate date) {
        ApiResponse<FixtureItem> res = restClient.get()
                .uri(u -> u.path("/fixtures")
                        .queryParam("date", date.toString())
                        .queryParam("timezone", "Asia/Seoul")
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        return res == null || res.response() == null ? List.of() : res.response();
    }

    public List<StandingItem> standings(int leagueId, int season) {
        ApiResponse<StandingsItem> res = restClient.get()
                .uri(u -> u.path("/standings")
                        .queryParam("league", leagueId)
                        .queryParam("season", season)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        if (res == null || res.response() == null || res.response().isEmpty()) return List.of();
        var groups = res.response().getFirst().league().standings();
        // 컵대회는 그룹이 여러 개 → 모두 펼쳐서 반환
        return groups == null ? List.of() : groups.stream().flatMap(List::stream).toList();
    }
}
