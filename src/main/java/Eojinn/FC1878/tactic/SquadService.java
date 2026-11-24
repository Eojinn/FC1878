package Eojinn.FC1878.tactic;

import Eojinn.FC1878.external.FootballApiProperties;
import Eojinn.FC1878.external.FootballDataClient;
import Eojinn.FC1878.external.FootballDataDtos.SquadPlayer;
import Eojinn.FC1878.external.FootballDataDtos.TeamDetail;
import Eojinn.FC1878.football.domain.Team;
import Eojinn.FC1878.football.repository.TeamRepository;
import Eojinn.FC1878.global.error.BusinessException;
import Eojinn.FC1878.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 전술판에서 쓰는 팀 스쿼드.
 * football-data.org 는 분당 10회 제한이 있어서 팀마다 12시간 동안 메모리에 저장해 두고 씀.
 * API 키가 없거나(샘플 데이터) 불러오기에 실패하면 빈 목록 → 화면에서 번호만 있는 선수로 표시.
 */
@Service
@RequiredArgsConstructor
public class SquadService {

    private static final Duration CACHE_TTL = Duration.ofHours(12);
    private static final Duration FAILURE_TTL = Duration.ofMinutes(2);

    private final TeamRepository teamRepository;
    private final FootballDataClient client;
    private final FootballApiProperties props;
    private final Map<Long, Cached> cache = new ConcurrentHashMap<>();

    public List<SquadPlayerResponse> squad(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT, "팀을 찾을 수 없어요."));
        if (team.getExternalId() == null || props.key() == null || props.key().isBlank()) return List.of();

        Cached hit = cache.get(teamId);
        if (hit != null && hit.expiresAt().isAfter(Instant.now())) return hit.players();

        TeamDetail detail = client.team(team.getExternalId());
        if (detail == null || detail.squad() == null) {
            cache.put(teamId, new Cached(List.of(), Instant.now().plus(FAILURE_TTL)));
            return List.of();
        }
        List<SquadPlayerResponse> players = detail.squad().stream()
                .map(SquadService::toResponse)
                .sorted(Comparator.comparing((SquadPlayerResponse p) -> roleOrder(p.role()))
                        .thenComparing(p -> p.number() == null ? 999 : p.number()))
                .toList();
        cache.put(teamId, new Cached(players, Instant.now().plus(CACHE_TTL)));
        return players;
    }

    private static SquadPlayerResponse toResponse(SquadPlayer p) {
        return new SquadPlayerResponse(p.id(), p.name(), p.shirtNumber(), role(p.position()), p.position(), p.nationality());
    }

    /** football-data 포지션 → GK / DF / MF / FW */
    static String role(String position) {
        if (position == null) return "MF";
        String s = position.toLowerCase(Locale.ROOT);
        if (s.contains("goalkeeper")) return "GK";
        if (s.contains("back") || s.contains("defen")) return "DF";
        if (s.contains("forward") || s.contains("offence") || s.contains("winger") || s.contains("striker")) return "FW";
        return "MF";
    }

    private static int roleOrder(String role) {
        return switch (role) {
            case "GK" -> 0;
            case "DF" -> 1;
            case "MF" -> 2;
            default -> 3;
        };
    }

    public record SquadPlayerResponse(Integer id, String name, Integer number, String role, String position,
                                      String nationality) {
    }

    private record Cached(List<SquadPlayerResponse> players, Instant expiresAt) {
    }
}
