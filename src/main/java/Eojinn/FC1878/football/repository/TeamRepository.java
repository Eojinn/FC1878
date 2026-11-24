package Eojinn.FC1878.football.repository;

import Eojinn.FC1878.football.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByExternalId(Integer externalId);

    /** 영문 이름이나 한글 이름에 검색어가 들어간 팀 */
    @org.springframework.data.jpa.repository.Query("""
            select t from Team t
            where lower(t.name) like lower(concat('%', :q, '%'))
               or lower(replace(t.name, ' ', '')) like lower(concat('%', replace(:q, ' ', ''), '%'))
               or replace(t.koreanName, ' ', '') like concat('%', replace(:q, ' ', ''), '%')
            order by t.name
            """)
    java.util.List<Team> search(@org.springframework.data.repository.query.Param("q") String q,
                                org.springframework.data.domain.Pageable pageable);
}
