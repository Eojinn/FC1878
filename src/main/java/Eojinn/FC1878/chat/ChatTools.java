package Eojinn.FC1878.chat;

import Eojinn.FC1878.career.CareerDtos.CareerResponse;
import Eojinn.FC1878.career.CareerService;
import Eojinn.FC1878.chat.ChatDtos.ChatLink;
import Eojinn.FC1878.external.FootballApiProperties;
import Eojinn.FC1878.football.dto.FootballDtos.FixtureResponse;
import Eojinn.FC1878.football.dto.FootballDtos.LeagueFixturesResponse;
import Eojinn.FC1878.football.service.FootballQueryService;
import Eojinn.FC1878.global.common.TimeUtil;
import Eojinn.FC1878.global.error.BusinessException;
import Eojinn.FC1878.overseas.OverseasDtos.PlayerResponse;
import Eojinn.FC1878.overseas.OverseasPlayerRepository;
import Eojinn.FC1878.search.SearchService;
import Eojinn.FC1878.search.SearchService.SearchResponse;
import Eojinn.FC1878.team.ProfileDtos.PlayerProfileResponse;
import Eojinn.FC1878.team.ProfileDtos.TeamProfileResponse;
import Eojinn.FC1878.team.ProfileService;
import Eojinn.FC1878.transfer.TransferDtos.TransfersResponse;
import Eojinn.FC1878.transfer.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * 챗봇이 쓸 수 있는 "도구" = 우리 사이트 DB 조회 기능 (읽기 전용).
 * AI 는 질문을 보고 필요한 도구를 골라 호출하고, 그 결과로만 답함 (기록을 지어내지 않도록).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatTools {

    private final SearchService searchService;
    private final ProfileService profileService;
    private final FootballQueryService queryService;
    private final OverseasPlayerRepository overseasRepository;
    private final TransferService transferService;
    private final CareerService careerService;
    private final FootballApiProperties footballProps;

    /** Gemini functionDeclarations */
    public List<Map<String, Object>> declarations() {
        return List.of(
                fn("search_football", "팀·선수 이름(한글·영문 모두 가능)으로 검색. 팀 ID·선수 ID 를 모를 때 가장 먼저 사용. "
                                + "결과의 link 가 /players/{id} 면 그 숫자가 선수 ID, /teams/{id} 면 팀 ID.",
                        props(p("query", "STRING", "검색어. 예: 손흥민, Arsenal, 맨시티")), List.of("query")),
                fn("get_team", "팀 정보: 감독·홈구장, 리그 순위, 최근 경기 결과, 다음 경기 일정, 선수단(선수별 경기·골·도움).",
                        props(p("team_id", "INTEGER", "팀 ID")), List.of("team_id")),
                fn("get_player", "선수 정보: 소속팀, 포지션, 나이, 국적, 대회별 이번 시즌 기록(경기·골·도움), 최근 이적.",
                        props(p("player_id", "INTEGER", "선수 ID")), List.of("player_id")),
                fn("get_fixtures", "날짜별 경기 일정·결과 (한국 시간 기준). 리그·팀으로 거를 수 있음.",
                        props(p("date", "STRING", "시작 날짜 YYYY-MM-DD (한국 시간)"),
                                p("days", "INTEGER", "며칠 치를 볼지 1~7 (기본 1)"),
                                p("league_id", "INTEGER", "리그 ID (list_leagues 로 확인)"),
                                p("team_id", "INTEGER", "팀 ID")),
                        List.of("date")),
                fn("list_leagues", "사이트에서 다루는 리그·대회 목록과 리그 ID.", props(), List.of()),
                fn("get_standings", "리그 순위표 (순위·경기·승무패·득실·승점·최근 5경기).",
                        props(p("league_id", "INTEGER", "리그 ID"),
                                p("season", "INTEGER", "시즌 시작 연도. 2026-27 시즌이면 2026. 생략하면 이번 시즌")),
                        List.of("league_id")),
                fn("get_overseas_players", "해외 리그에서 뛰는 한국 선수(해외파) 목록: 소속팀, 리그, 나라, 임대 여부.",
                        props(), List.of()),
                fn("get_transfers", "확정된 이적 소식 (최신순). 선수·팀 이름으로 찾거나 한국 선수만 볼 수 있음.",
                        props(p("keyword", "STRING", "선수 또는 팀 이름 (생략하면 최근 이적시장 전체)"),
                                p("korean_only", "BOOLEAN", "한국 선수만")),
                        List.of()),
                fn("get_career", "선수 커리어(Wikidata): 지금까지 거친 팀과 팀별 기간·경기·골, 국가대표 기록. 은퇴한 선수도 가능.",
                        props(p("name", "STRING", "선수 이름 (한글 또는 영문)"),
                                p("korean", "BOOLEAN", "한국 선수면 true")),
                        List.of("name"))
        );
    }

    /** 도구 실행. 결과는 Gemini functionResponse.response 에 들어갈 객체. 화면에 보여줄 바로가기는 links 에 모음 */
    public Map<String, Object> execute(String name, Map<String, Object> args, Set<ChatLink> links) {
        try {
            Object result = switch (name) {
                case "search_football" -> search(str(args, "query"), links);
                case "get_team" -> team(num(args, "team_id"), links);
                case "get_player" -> player(num(args, "player_id"), links);
                case "get_fixtures" -> fixtures(args, links);
                case "list_leagues" -> queryService.leagues();
                case "get_standings" -> standings(args, links);
                case "get_overseas_players" -> overseas(links);
                case "get_transfers" -> transfers(args, links);
                case "get_career" -> career(args, links);
                default -> Map.of("error", "없는 도구: " + name);
            };
            return Map.of("result", result == null ? Map.of() : result);
        } catch (BusinessException e) {
            return Map.of("error", e.getMessage());
        } catch (IllegalArgumentException e) {
            return Map.of("error", "입력값 오류: " + e.getMessage());
        } catch (RuntimeException e) {
            log.warn("챗봇 도구 {} 실패: {}", name, e.toString());
            return Map.of("error", "데이터를 불러오지 못했어요.");
        }
    }

    private SearchResponse search(String query, Set<ChatLink> links) {
        SearchResponse r = searchService.search(query, 8);
        r.teams().stream().limit(2).forEach(t -> links.add(new ChatLink("team", t.name(), t.link())));
        r.players().stream().limit(2).forEach(p -> links.add(new ChatLink("player", p.name(), p.link())));
        return r;
    }

    private TeamProfileResponse team(Long teamId, Set<ChatLink> links) {
        TeamProfileResponse r = profileService.team(teamId);
        links.add(new ChatLink("team", r.team().name(), "/teams/" + r.team().id()));
        return r;
    }

    private PlayerProfileResponse player(Long playerId, Set<ChatLink> links) {
        PlayerProfileResponse r = profileService.player(playerId);
        links.add(new ChatLink("player", r.koreanName() != null ? r.koreanName() : r.name(), "/players/" + r.id()));
        return r;
    }

    private Map<String, Object> fixtures(Map<String, Object> args, Set<ChatLink> links) {
        LocalDate from;
        try {
            from = LocalDate.parse(str(args, "date"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("date 는 YYYY-MM-DD 형식이어야 해요.");
        }
        int days = Math.max(1, Math.min(7, Optional.ofNullable(numOrNull(args, "days")).map(Long::intValue).orElse(1)));
        Long leagueId = numOrNull(args, "league_id");
        Long teamId = numOrNull(args, "team_id");

        Map<String, Object> byDate = new LinkedHashMap<>();
        List<FixtureResponse> all = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate d = from.plusDays(i);
            List<LeagueFixturesResponse> list = queryService.fixturesByDate(d, leagueId, teamId);
            if (!list.isEmpty()) byDate.put(d.toString(), list);
            list.forEach(g -> all.addAll(g.fixtures()));
        }
        if (all.size() <= 3) {
            all.forEach(f -> links.add(new ChatLink("fixture", f.home().name() + " vs " + f.away().name(), "/fixtures/" + f.id())));
        } else {
            links.add(new ChatLink("page", "경기일정 " + from, "/fixtures?date=" + from));
        }
        return Map.of("timezone", "Asia/Seoul", "fixturesByDate", byDate, "count", all.size());
    }

    private Object standings(Map<String, Object> args, Set<ChatLink> links) {
        Long leagueId = num(args, "league_id");
        Long season = numOrNull(args, "season");
        Integer s = season != null ? season.intValue() : footballProps.season();
        links.add(new ChatLink("page", "순위표", "/standings?league=" + leagueId));
        return Map.of("season", s, "table", queryService.standings(leagueId, s));
    }

    private Object overseas(Set<ChatLink> links) {
        links.add(new ChatLink("page", "해외파 지도", "/overseas"));
        return overseasRepository.findAllByOrderBySortOrderAscIdAsc().stream().map(PlayerResponse::from).toList();
    }

    private Object transfers(Map<String, Object> args, Set<ChatLink> links) {
        String keyword = str(args, "keyword");
        boolean korean = Boolean.TRUE.equals(args.get("korean_only"));
        // 이름으로 찾을 때는 모든 이적시장에서, 아니면 가장 최근 이적시장
        String window = keyword != null && !keyword.isBlank() ? TransferService.ALL : null;
        TransfersResponse r = transferService.search(window, null, korean, keyword, "date");
        links.add(new ChatLink("page", "이적시장", "/transfers"));
        return Map.of("window", String.valueOf(r.window()), "summary", r.summary(),
                "items", r.items().stream().limit(20).toList());
    }

    private Object career(Map<String, Object> args, Set<ChatLink> links) {
        String name = str(args, "name");
        boolean korean = Boolean.TRUE.equals(args.get("korean"));
        boolean hangul = name != null && name.codePoints().anyMatch(c -> c >= 0xAC00 && c <= 0xD7A3);
        CareerResponse r = careerService.lookup(name, hangul ? "ko" : "en", null, korean || hangul);
        if (r.found()) {
            String label = r.koreanName() != null ? r.koreanName() : r.name();
            links.add(new ChatLink("player", label + " 커리어",
                    "/career?name=" + java.net.URLEncoder.encode(name, java.nio.charset.StandardCharsets.UTF_8)
                            + "&lang=" + (hangul ? "ko" : "en") + (korean || hangul ? "&korea=true" : "")));
        }
        return r;
    }

    /** 오늘 날짜 등 기본 정보 (시스템 안내문에 넣음) */
    public String today() {
        LocalDate d = LocalDate.now(TimeUtil.KST);
        String[] week = {"월", "화", "수", "목", "금", "토", "일"};
        return d + " (" + week[d.getDayOfWeek().getValue() - 1] + ")";
    }

    public Integer season() {
        return footballProps.season();
    }

    /* ===== 작은 도우미 ===== */

    private static Map<String, Object> fn(String name, String description, Map<String, Object> properties, List<String> required) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("type", "OBJECT");
        params.put("properties", properties);
        if (!required.isEmpty()) params.put("required", required);
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("name", name);
        f.put("description", description);
        f.put("parameters", params);
        return f;
    }

    @SafeVarargs
    private static Map<String, Object> props(Map.Entry<String, Object>... entries) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : entries) m.put(e.getKey(), e.getValue());
        return m;
    }

    private static Map.Entry<String, Object> p(String name, String type, String description) {
        return Map.entry(name, Map.of("type", type, "description", description)); // Gemini Schema 타입: STRING, INTEGER, BOOLEAN …
    }

    private static String str(Map<String, Object> args, String key) {
        Object v = args == null ? null : args.get(key);
        return v == null ? null : v.toString().trim();
    }

    private static Long num(Map<String, Object> args, String key) {
        Long v = numOrNull(args, key);
        if (v == null) throw new IllegalArgumentException(key + " 가 필요해요.");
        return v;
    }

    private static Long numOrNull(Map<String, Object> args, String key) {
        Object v = args == null ? null : args.get(key);
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        try {
            return (long) Double.parseDouble(v.toString().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(key + " 는 숫자여야 해요.");
        }
    }
}
