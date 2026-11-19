package Eojinn.FC1878.football.repository;

import Eojinn.FC1878.football.domain.Fixture;
import Eojinn.FC1878.football.domain.FixtureStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FixtureRepository extends JpaRepository<Fixture, Long> {

    Optional<Fixture> findByExternalId(Integer externalId);

    @Query("""
            select f from Fixture f
              join fetch f.league join fetch f.homeTeam join fetch f.awayTeam
            where f.kickoffAt >= :from and f.kickoffAt < :to
              and (:leagueId is null or f.league.id = :leagueId)
              and (:teamId is null or f.homeTeam.id = :teamId or f.awayTeam.id = :teamId)
            order by f.league.id, f.kickoffAt
            """)
    List<Fixture> search(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
                         @Param("leagueId") Long leagueId, @Param("teamId") Long teamId);

    @Query("""
            select f from Fixture f
              join fetch f.league join fetch f.homeTeam join fetch f.awayTeam
            where f.id = :id
            """)
    Optional<Fixture> findDetail(@Param("id") Long id);

    List<Fixture> findByStatusAndSettledFalse(FixtureStatus status);

    @Query("""
            select f from Fixture f
              join fetch f.league join fetch f.homeTeam join fetch f.awayTeam
            where f.status = :status and f.kickoffAt > :now
            order by f.kickoffAt
            """)
    List<Fixture> findUpcoming(@Param("status") FixtureStatus status, @Param("now") LocalDateTime now,
                               org.springframework.data.domain.Pageable pageable);
}
