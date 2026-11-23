package Eojinn.FC1878.overseas;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OverseasPlayerRepository extends JpaRepository<OverseasPlayer, Long> {

    List<OverseasPlayer> findAllByOrderBySortOrderAscIdAsc();
}
