package Eojinn.FC1878.football.repository;

import Eojinn.FC1878.football.domain.League;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeagueRepository extends JpaRepository<League, Long> {
    Optional<League> findByExternalId(Integer externalId);
}
