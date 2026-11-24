package Eojinn.FC1878.team;

import Eojinn.FC1878.external.FootballApiProperties;
import Eojinn.FC1878.external.FootballDataDtos.SquadPlayer;
import Eojinn.FC1878.external.FootballDataDtos.TeamDetail;
import Eojinn.FC1878.football.domain.FixtureStatus;
import Eojinn.FC1878.football.domain.Standing;
import Eojinn.FC1878.football.domain.Team;
import Eojinn.FC1878.football.dto.FootballDtos.FixtureResponse;
import Eojinn.FC1878.football.dto.FootballDtos.LeagueResponse;
import Eojinn.FC1878.football.dto.FootballDtos.TeamResponse;
import Eojinn.FC1878.football.repository.FixtureRepository;
import Eojinn.FC1878.football.repository.StandingRepository;
import Eojinn.FC1878.football.repository.TeamRepository;
import Eojinn.FC1878.global.common.TimeUtil;
import Eojinn.FC1878.global.error.BusinessException;
import Eojinn.FC1878.global.error.ErrorCode;
import Eojinn.FC1878.player.*;
import Eojinn.FC1878.team.ProfileDtos.*;
import Eojinn.FC1878.transfer.Transfer;
import Eojinn.FC1878.transfer.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

/** 팀 페이지·선수 페이지에 필요한 정보를 한 번에 모아서 줌 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final TeamRepository teamRepository;
    private final StandingRepository standingRepository;
    private final FixtureRepository fixtureRepository;
    private final PlayerRepository playerRepository;
    private final PlayerStatRepository statRepository;
    private final TransferRepository transferRepository;
    private final TeamDetailCache teamDetailCache;
    private final FootballApiProperties props;

    /* ===== 팀 ===== */

    public TeamProfileResponse team(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT, "팀을 찾을 수 없어요."));
        TeamDetail detail = teamDetailCache.get(team);

        TeamInfo info = detail == null ? null : new TeamInfo(detail.venue(), detail.founded(),
                detail.coach() == null ? null : detail.coach().name(),
                detail.coach() == null ? null : detail.coach().nationality(),
                detail.clubColors(), detail.website(), detail.address());

        // 순위: 가장 최근 시즌만
        List<Standing> rows = standingRepository.findByTeam(teamId);
        Integer latest = rows.isEmpty() ? null : rows.get(0).getSeason();
        List<TeamStanding> standings = rows.stream()
                .filter(s -> Objects.equals(s.getSeason(), latest))
                .map(s -> new TeamStanding(LeagueResponse.from(s.getLeague()), s.getSeason(), s.getRank(), s.getPlayed(),
                        s.getWin(), s.getDraw(), s.getLose(), s.getGoalsFor(), s.getGoalsAgainst(), s.getGoalDiff(),
                        s.getPoints(), s.getForm()))
                .toList();

        List<FixtureResponse> recent = fixtureRepository.findByTeam(teamId, List.of(FixtureStatus.FINISHED),
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "kickoffAt")))
                .stream().map(FixtureResponse::from).toList();
        List<FixtureResponse> upcoming = fixtureRepository.findByTeam(teamId,
                        List.of(FixtureStatus.LIVE, FixtureStatus.SCHEDULED),
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "kickoffAt")))
                .stream().map(FixtureResponse::from).toList();

        Integer season = props.season();
        List<SquadMember> squad = squad(teamId, detail, season);

        return new TeamProfileResponse(TeamResponse.from(team),
                Objects.equals(team.getDisplayName(), team.getName()) ? null : team.getName(),
                info, standings, recent, upcoming, squad, season);
    }

    private List<SquadMember> squad(Long teamId, TeamDetail detail, Integer season) {
        List<Player> players = playerRepository.findByTeamId(teamId);
        Comparator<SquadMember> order = Comparator.comparing((SquadMember m) -> PlayerRole.order(m.role()))
                .thenComparing(m -> m.number() == null ? 999 : m.number())
                .thenComparing(SquadMember::name);

        if (players.isEmpty()) { // 선수 동기화 전 → API 스쿼드 그대로 (기록 없음)
            if (detail == null || detail.squad() == null) return List.of();
            return detail.squad().stream()
                    .map(sp -> new SquadMember(null, sp.name(), null, sp.shirtNumber(), PlayerRole.of(sp.position()),
                            sp.position(), sp.nationality(), age(Player.parseDate(sp.dateOfBirth())), null, null, null))
                    .sorted(order).toList();
        }

        // 이번 시즌 기록을 선수별로 합침 (리그 + 챔스)
        Map<Long, List<PlayerStat>> stats = statRepository
                .findByPlayersAndSeason(players.stream().map(Player::getId).toList(), season).stream()
                .collect(Collectors.groupingBy(s -> s.getPlayer().getId()));
        return players.stream().map(p -> {
            List<PlayerStat> list = stats.getOrDefault(p.getId(), List.of());
            return new SquadMember(p.getId(), p.getName(), p.getKoreanName(), p.getShirtNumber(), p.getRole(), p.getPosition(),
                    p.getNationality(), age(p.getDateOfBirth()),
                    sum(list, PlayerStat::getPlayedMatches), list.isEmpty() ? null : list.stream().mapToInt(PlayerStat::getGoals).sum(),
                    sum(list, PlayerStat::getAssists));
        }).sorted(order).toList();
    }

    /* ===== 선수 ===== */

    public PlayerProfileResponse player(Long playerId) {
        Player p = playerRepository.findById(playerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT, "선수를 찾을 수 없어요."));
        List<PlayerSeasonStat> stats = statRepository.findByPlayer(playerId).stream()
                .map(s -> new PlayerSeasonStat(LeagueResponse.from(s.getLeague()), s.getSeason(), s.getPlayedMatches(),
                        s.getGoals(), s.getAssists(), s.getPenalties()))
                .toList();
        List<RecentTransfer> transfers = transferRepository.findTop5ByPlayerNameEnIgnoreCaseOrderByTransferDateDesc(p.getName())
                .stream().map(ProfileService::transfer).toList();
        Team team = p.getTeam();
        return new PlayerProfileResponse(p.getId(), p.getName(), p.getKoreanName(), p.getRole(), p.getPosition(), p.getShirtNumber(),
                p.getNationality(), p.getDateOfBirth(), age(p.getDateOfBirth()),
                team == null ? null : TeamResponse.from(team), p.getTeamName(), p.getTeamLogoUrl(), stats, transfers);
    }

    private static RecentTransfer transfer(Transfer t) {
        String fee = t.getFeeMillionEur() == null ? null
                : "€" + t.getFeeMillionEur().stripTrailingZeros().toPlainString() + "M";
        return new RecentTransfer(t.getFromClub(), t.getToClub(), t.getType().name(), t.getTransferDate(), fee);
    }

    /* ===== 공통 ===== */

    private static Integer age(LocalDate birth) {
        return birth == null ? null : Period.between(birth, LocalDate.now(TimeUtil.KST)).getYears();
    }

    private static Integer sum(List<PlayerStat> list, java.util.function.Function<PlayerStat, Integer> f) {
        List<Integer> values = list.stream().map(f).filter(Objects::nonNull).toList();
        return values.isEmpty() ? null : values.stream().mapToInt(Integer::intValue).sum();
    }
}
