package Eojinn.FC1878.football.service;

import Eojinn.FC1878.football.domain.Fixture;
import Eojinn.FC1878.football.dto.FootballDtos.*;
import Eojinn.FC1878.football.repository.FixtureRepository;
import Eojinn.FC1878.football.repository.LeagueRepository;
import Eojinn.FC1878.football.repository.StandingRepository;
import Eojinn.FC1878.global.error.BusinessException;
import Eojinn.FC1878.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FootballQueryService {

    private final LeagueRepository leagueRepository;
    private final FixtureRepository fixtureRepository;
    private final StandingRepository standingRepository;
    private final Eojinn.FC1878.football.repository.TeamRepository teamRepository;

    public List<LeagueResponse> leagues() {
        return leagueRepository.findAll().stream().map(LeagueResponse::from).toList();
    }

    /** 특정 날짜(KST)의 경기를 리그별로 묶어서 반환 */
    public List<LeagueFixturesResponse> fixturesByDate(LocalDate date, Long leagueId, Long teamId) {
        List<Fixture> fixtures = fixtureRepository.search(
                date.atStartOfDay(), date.plusDays(1).atStartOfDay(), leagueId, teamId);

        Map<Long, LeagueFixturesResponse> grouped = new LinkedHashMap<>();
        for (Fixture f : fixtures) {
            grouped.computeIfAbsent(f.getLeague().getId(),
                            k -> new LeagueFixturesResponse(LeagueResponse.from(f.getLeague()), new java.util.ArrayList<>()))
                    .fixtures().add(FixtureResponse.from(f));
        }
        return List.copyOf(grouped.values());
    }

    /** 메인 "오늘의 승부예측" 카드: 가장 가까운 예정 경기 */
    public java.util.Optional<FixtureResponse> featured() {
        return fixtureRepository.findUpcoming(Eojinn.FC1878.football.domain.FixtureStatus.SCHEDULED,
                        Eojinn.FC1878.global.common.TimeUtil.nowKst(),
                        org.springframework.data.domain.PageRequest.of(0, 1))
                .stream().findFirst().map(FixtureResponse::from);
    }

    public List<TeamResponse> teams() {
        return teamRepository.findAll().stream().map(TeamResponse::from)
                .sorted(java.util.Comparator.comparing(TeamResponse::name)).toList();
    }

    public FixtureResponse fixture(Long id) {
        return fixtureRepository.findDetail(id).map(FixtureResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.FIXTURE_NOT_FOUND));
    }

    public List<StandingResponse> standings(Long leagueId, Integer season) {
        if (!leagueRepository.existsById(leagueId)) throw new BusinessException(ErrorCode.LEAGUE_NOT_FOUND);
        return standingRepository.findTable(leagueId, season).stream().map(StandingResponse::from).toList();
    }
}
