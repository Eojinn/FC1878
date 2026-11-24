package Eojinn.FC1878.team;

import Eojinn.FC1878.external.FootballApiProperties;
import Eojinn.FC1878.external.FootballDataClient;
import Eojinn.FC1878.external.FootballDataDtos.TeamDetail;
import Eojinn.FC1878.football.domain.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * football-data.org 팀 상세(감독·경기장·창단·스쿼드)를 팀마다 12시간 저장해 두고 씀.
 * 키가 없거나 실패하면 null (실패는 2분 뒤 다시 시도).
 */
@Component
@RequiredArgsConstructor
public class TeamDetailCache {

    private static final Duration TTL = Duration.ofHours(12);
    private static final Duration FAILURE_TTL = Duration.ofMinutes(2);

    private final FootballDataClient client;
    private final FootballApiProperties props;
    private final Map<Long, Entry> cache = new ConcurrentHashMap<>();

    public TeamDetail get(Team team) {
        if (team.getExternalId() == null || props.key() == null || props.key().isBlank()) return null;
        Entry hit = cache.get(team.getId());
        if (hit != null && hit.expiresAt().isAfter(Instant.now())) return hit.detail();
        TeamDetail detail;
        try {
            detail = client.team(team.getExternalId());
        } catch (Exception e) { // 연결 문제 등이 있어도 팀 페이지의 나머지(순위·경기·선수단)는 보여 줌
            detail = null;
        }
        cache.put(team.getId(), new Entry(detail, Instant.now().plus(detail == null ? FAILURE_TTL : TTL)));
        return detail;
    }

    private record Entry(TeamDetail detail, Instant expiresAt) {
    }
}
