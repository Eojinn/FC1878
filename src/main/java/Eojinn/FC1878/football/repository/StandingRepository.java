package Eojinn.FC1878.football.repository;

import Eojinn.FC1878.football.domain.Standing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StandingRepository extends JpaRepository<Standing, Long> {

    Optional<Standing> findByLeagueIdAndSeasonAndTeamId(Long leagueId, Integer season, Long teamId);

    /** 한 팀의 리그 순위 (최근 시즌 먼저) */
    @Query("""
            select s from Standing s join fetch s.league
            where s.team.id = :teamId
            order by s.season desc, s.league.id
            """)
    List<Standing> findByTeam(@Param("teamId") Long teamId);

    @Query("""
            select s from Standing s join fetch s.team
            where s.league.id = :leagueId and s.season = :season
            order by s.rank
            """)
    List<Standing> findTable(@Param("leagueId") Long leagueId, @Param("season") Integer season);
}
