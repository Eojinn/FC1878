package Eojinn.FC1878.tactic;

import Eojinn.FC1878.external.FootballDataDtos.SquadPlayer;
import Eojinn.FC1878.external.FootballDataDtos.TeamDetail;
import Eojinn.FC1878.football.domain.Team;
import Eojinn.FC1878.football.repository.TeamRepository;
import Eojinn.FC1878.global.error.BusinessException;
import Eojinn.FC1878.global.error.ErrorCode;
import Eojinn.FC1878.player.Player;
import Eojinn.FC1878.player.PlayerRepository;
import Eojinn.FC1878.player.PlayerRole;
import Eojinn.FC1878.team.TeamDetailCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * 전술판에서 쓰는 팀 스쿼드.
 * DB(하루 한 번 동기화)에 있으면 그걸, 없으면 football-data.org 팀 상세(12시간 캐시)에서.
 * 키가 없거나(샘플 데이터) 실패하면 빈 목록 → 화면에서 번호만 있는 선수로 표시.
 */
@Service
@RequiredArgsConstructor
public class SquadService {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final TeamDetailCache teamDetailCache;

    public List<SquadPlayerResponse> squad(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT, "팀을 찾을 수 없어요."));

        // 하루 한 번 동기화한 선수 목록이 있으면 API 를 부르지 않고 그걸 씀
        List<Player> stored = playerRepository.findByTeamId(teamId);
        if (!stored.isEmpty()) {
            return stored.stream()
                    .map(p -> new SquadPlayerResponse(p.getExternalId(), p.getName(), p.getShirtNumber(), p.getRole(),
                            p.getPosition(), p.getNationality()))
                    .sorted(ORDER)
                    .toList();
        }
        TeamDetail detail = teamDetailCache.get(team);
        if (detail == null || detail.squad() == null) return List.of();
        return detail.squad().stream().map(SquadService::toResponse).sorted(ORDER).toList();
    }

    private static final Comparator<SquadPlayerResponse> ORDER =
            Comparator.comparing((SquadPlayerResponse p) -> PlayerRole.order(p.role()))
                    .thenComparing(p -> p.number() == null ? 999 : p.number());

    private static SquadPlayerResponse toResponse(SquadPlayer p) {
        return new SquadPlayerResponse(p.id(), p.name(), p.shirtNumber(), PlayerRole.of(p.position()), p.position(), p.nationality());
    }

    public record SquadPlayerResponse(Integer id, String name, Integer number, String role, String position,
                                      String nationality) {
    }
}
