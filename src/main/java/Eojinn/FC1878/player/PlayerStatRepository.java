package Eojinn.FC1878.player;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PlayerStatRepository extends JpaRepository<PlayerStat, Long> {

    Optional<PlayerStat> findByPlayerIdAndLeagueIdAndSeason(Long playerId, Long leagueId, Integer season);

    @Query("select s from PlayerStat s join fetch s.league where s.player.id = :playerId order by s.season desc, s.goals desc")
    List<PlayerStat> findByPlayer(@Param("playerId") Long playerId);

    @Query("select s from PlayerStat s join fetch s.league where s.player.id in :playerIds and s.season = :season")
    List<PlayerStat> findByPlayersAndSeason(@Param("playerIds") Collection<Long> playerIds, @Param("season") Integer season);
}
