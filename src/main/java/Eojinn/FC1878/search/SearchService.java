package Eojinn.FC1878.search;

import Eojinn.FC1878.football.domain.Team;
import Eojinn.FC1878.football.repository.TeamRepository;
import Eojinn.FC1878.global.error.BusinessException;
import Eojinn.FC1878.global.error.ErrorCode;
import Eojinn.FC1878.overseas.OverseasPlayer;
import Eojinn.FC1878.overseas.OverseasPlayerRepository;
import Eojinn.FC1878.player.Player;
import Eojinn.FC1878.player.PlayerRepository;
import Eojinn.FC1878.transfer.Transfer;
import Eojinn.FC1878.transfer.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 헤더 검색: 팀 + 선수.
 * 선수는 세 곳에서 찾음 → 스쿼드(영문 이름), 해외파(한글 이름), 이적시장(한글·영문 이름)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final OverseasPlayerRepository overseasRepository;
    private final TransferRepository transferRepository;

    public SearchResponse search(String keyword, int limit) {
        String q = keyword == null ? "" : keyword.trim();
        if (q.isEmpty()) return new SearchResponse(q, List.of(), List.of());
        if (q.length() > 40) throw new BusinessException(ErrorCode.INVALID_INPUT, "검색어는 40자까지 입력할 수 있어요.");
        int size = Math.max(1, Math.min(limit, 30));

        List<TeamResult> teams = teamRepository.search(q, PageRequest.of(0, size)).stream()
                .map(SearchService::team)
                .toList();

        // 같은 선수가 여러 곳에서 나오면 한 번만 (한글 이름 결과를 먼저)
        Map<String, PlayerResult> players = new LinkedHashMap<>();
        for (OverseasPlayer o : overseasRepository.findTop10ByNameContainingOrderBySortOrder(q)) {
            players.putIfAbsent(key(o.getName()), new PlayerResult(o.getName(), o.getClub() + " · " + o.getLeague(),
                    o.getPosition().name(), o.getClub(), null, null, "대한민국", "OVERSEAS", "/overseas"));
        }
        for (Transfer t : transferRepository
                .findTop10ByPlayerNameContainingOrPlayerNameEnContainingIgnoreCaseOrderByTransferDateDesc(q, q)) {
            PlayerResult r = new PlayerResult(t.getPlayerName(),
                    "최근 이적: " + t.getFromClub() + " → " + t.getToClub(),
                    t.getPosition().name(), t.getToClub(), null, null, t.getNationality(), "TRANSFER",
                    "/transfers?window=all&q=" + URLEncoder.encode(t.getPlayerName(), StandardCharsets.UTF_8));
            players.putIfAbsent(key(t.getPlayerName()), r);
            if (t.getPlayerNameEn() != null) players.putIfAbsent(key(t.getPlayerNameEn()), r); // 영문 스쿼드 결과와 겹치지 않게
        }
        for (Player p : playerRepository.search(q, PageRequest.of(0, size))) {
            Long teamId = p.getTeam() != null ? p.getTeam().getId() : null;
            players.putIfAbsent(key(p.getName()), new PlayerResult(p.getName(),
                    Optional.ofNullable(p.getPosition()).orElse(""), p.getRole(), p.getTeamName(), teamId,
                    p.getTeamLogoUrl(), p.getNationality(), "SQUAD", "/players/" + p.getId()));
        }
        List<PlayerResult> playerList = new ArrayList<>(new LinkedHashSet<>(players.values()));
        return new SearchResponse(q, teams, playerList.subList(0, Math.min(size, playerList.size())));
    }

    private static TeamResult team(Team t) {
        String sub = t.getKoreanName() != null && !t.getKoreanName().isBlank() ? t.getName() : null;
        return new TeamResult(t.getId(), t.getDisplayName(), sub, t.getLogoUrl(), "/teams/" + t.getId());
    }

    private static String key(String name) {
        return name.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
    }

    public record SearchResponse(String query, List<TeamResult> teams, List<PlayerResult> players) {
    }

    /** link: 누르면 이동할 화면 (팀 → 팀 페이지) */
    public record TeamResult(Long id, String name, String subName, String logoUrl, String link) {
    }

    /** type: SQUAD(스쿼드), OVERSEAS(해외파), TRANSFER(이적시장) */
    public record PlayerResult(String name, String detail, String role, String teamName, Long teamId,
                               String teamLogoUrl, String nationality, String type, String link) {
    }
}
