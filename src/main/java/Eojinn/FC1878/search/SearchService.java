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
        NameQuery nq = NameQuery.of(q);
        if (nq.compact().isEmpty()) return new SearchResponse(q, List.of(), List.of());

        List<TeamResult> teams = teamRepository.search(q, PageRequest.of(0, size)).stream()
                .map(SearchService::team)
                .toList();

        // 같은 선수가 여러 곳에서 나오면 한 번만. 기록·커리어가 다 있는 스쿼드 선수를 먼저
        Map<String, PlayerResult> players = new LinkedHashMap<>();
        Set<String> seen = new HashSet<>();

        for (Player p : playerRepository.search(nq.compact(), nq.token(0), nq.token(1), nq.token(2), PageRequest.of(0, size))) {
            if (!seen.add(NameQuery.key(p.getName()))) continue;
            if (p.getKoreanName() != null) seen.add(NameQuery.key(p.getKoreanName()));
            Long teamId = p.getTeam() != null ? p.getTeam().getId() : null;
            String ko = p.getKoreanName();
            players.put("p" + p.getId(), new PlayerResult(ko != null ? ko : p.getName(), ko != null ? p.getName() : null,
                    Optional.ofNullable(p.getPosition()).orElse(""), p.getRole(), p.getTeamName(), teamId,
                    p.getTeamLogoUrl(), p.getNationality(), "SQUAD", "/players/" + p.getId()));
        }

        // 해외파 (한글 이름 + 영문 이름 + 다른 표기)
        for (OverseasPlayer o : overseasRepository.findAllByOrderBySortOrderAscIdAsc()) {
            if (!nq.matches(o.getName(), o.getNameEn(), o.getNameAliases())) continue;
            if (seen.contains(NameQuery.key(o.getName())) || seen.contains(NameQuery.key(o.getNameEn()))) continue;
            seen.add(NameQuery.key(o.getName()));
            seen.add(NameQuery.key(o.getNameEn()));
            players.put("o" + o.getId(), new PlayerResult(o.getName(), o.getNameEn(), o.getClub() + " · " + o.getLeague(),
                    o.getPosition().name(), o.getClub(), null, null, "대한민국", "OVERSEAS",
                    o.getNameEn() != null ? careerLink(o.getNameEn(), "en", true) : careerLink(o.getName(), "ko", true)));
        }

        // 이적시장 (한글 이름 + 영문 이름)
        for (Transfer t : transferRepository.findAllByOrderByTransferDateDescIdDesc()) {
            if (!nq.matches(t.getPlayerName(), t.getPlayerNameEn())) continue;
            if (seen.contains(NameQuery.key(t.getPlayerName())) || seen.contains(NameQuery.key(t.getPlayerNameEn()))) continue;
            seen.add(NameQuery.key(t.getPlayerName()));
            seen.add(NameQuery.key(t.getPlayerNameEn()));
            players.put("t" + t.getId(), new PlayerResult(t.getPlayerName(), t.getPlayerNameEn(),
                    "최근 이적: " + t.getFromClub() + " → " + t.getToClub(),
                    t.getPosition().name(), t.getToClub(), null, null, t.getNationality(), "TRANSFER",
                    t.getPlayerNameEn() != null ? careerLink(t.getPlayerNameEn(), "en", t.isKorean())
                            : careerLink(t.getPlayerName(), "ko", t.isKorean())));
        }

        List<PlayerResult> playerList = new ArrayList<>(players.values());
        return new SearchResponse(q, teams, playerList.subList(0, Math.min(size, playerList.size())));
    }

    private static TeamResult team(Team t) {
        // 한글 표시 이름이 있으면 영문을, 없으면 검색용 한글 이름을 함께 보여줌 (예: Man United · 맨체스터 유나이티드)
        String sub = t.getKoreanName() != null && !t.getKoreanName().isBlank() ? t.getName()
                : Eojinn.FC1878.football.service.TeamKoreanNames.primaryKorean(t.getSearchNames());
        return new TeamResult(t.getId(), t.getDisplayName(), sub, t.getLogoUrl(), "/teams/" + t.getId());
    }

    /** DB에 없는 선수 → 커리어 페이지 (/career?name=…) */
    static String careerLink(String name, String lang, boolean korea) {
        return "/career?name=" + URLEncoder.encode(name, StandardCharsets.UTF_8) + "&lang=" + lang
                + (korea ? "&korea=true" : "");
    }


    public record SearchResponse(String query, List<TeamResult> teams, List<PlayerResult> players) {
    }

    /** link: 누르면 이동할 화면 (팀 → 팀 페이지) */
    public record TeamResult(Long id, String name, String subName, String logoUrl, String link) {
    }

    /** name: 화면에 크게 보일 이름(한글이 있으면 한글), nameEn: 함께 보여 줄 영문 이름
     *  type: SQUAD(스쿼드), OVERSEAS(해외파), TRANSFER(이적시장) */
    public record PlayerResult(String name, String nameEn, String detail, String role, String teamName, Long teamId,
                               String teamLogoUrl, String nationality, String type, String link) {
    }
}
