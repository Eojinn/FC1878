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

    List<Player> findByExternalIdIn(java.util.Collection<Integer> externalIds);

    /**
     * 이름 검색 (대소문자·하이픈·띄어쓰기 무시, 단어 순서 무관, 한글 이름 포함)
     * compact: 공백·하이픈을 뺀 검색어 / t1~t3: 단어별 (없으면 null) / 예) "minjae kim" → "Kim Min-Jae" 도 찾음
     */
    @Query("""
            select p from Player p left join fetch p.team
            where lower(replace(replace(p.name, '-', ''), ' ', '')) like concat('%', :compact, '%')
               or (lower(replace(p.name, '-', '')) like concat('%', :t1, '%')
                   and (:t2 is null or lower(replace(p.name, '-', '')) like concat('%', :t2, '%'))
                   and (:t3 is null or lower(replace(p.name, '-', '')) like concat('%', :t3, '%')))
               or replace(p.koreanName, ' ', '') like concat('%', :compact, '%')
            order by case when lower(p.name) like concat(:t1, '%') or p.koreanName like concat(:compact, '%') then 0 else 1 end,
                     p.name
            """)
    List<Player> search(@Param("compact") String compact, @Param("t1") String t1, @Param("t2") String t2,
                        @Param("t3") String t3, Pageable pageable);

    /** 한글 이름을 아직 안 찾아봤거나, 못 찾은 지 오래된 선수 */
    @Query("""
            select p from Player p
            where p.koreanName is null and (p.koreanNameCheckedAt is null or p.koreanNameCheckedAt < :before)
            order by p.koreanNameCheckedAt asc nulls first, p.id
            """)
    List<Player> findNeedingKoreanName(@Param("before") LocalDateTime before, Pageable pageable);

    @Modifying
    @Query("delete from Player p where p.syncedAt < :before")
    int deleteNotSyncedSince(@Param("before") LocalDateTime before);
}
