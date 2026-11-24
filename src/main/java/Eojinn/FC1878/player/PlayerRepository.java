package Eojinn.FC1878.player;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByExternalId(Integer externalId);

    List<Player> findByTeamId(Long teamId);

    /** 이름에 검색어가 들어간 선수 (앞부분이 맞는 선수 먼저) */
    @Query("""
            select p from Player p left join fetch p.team
            where lower(p.name) like lower(concat('%', :q, '%'))
            order by case when lower(p.name) like lower(concat(:q, '%')) then 0 else 1 end, p.name
            """)
    List<Player> search(@Param("q") String q, Pageable pageable);

    @Modifying
    @Query("delete from Player p where p.syncedAt < :before")
    int deleteNotSyncedSince(@Param("before") LocalDateTime before);
}
