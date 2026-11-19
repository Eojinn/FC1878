package Eojinn.FC1878.football.repository;

import Eojinn.FC1878.football.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByExternalId(Integer externalId);
}
