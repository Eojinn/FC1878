package Eojinn.FC1878.prediction;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    Optional<Prediction> findByMemberIdAndFixtureId(Long memberId, Long fixtureId);

    @Query("select p.pick, count(p) from Prediction p where p.fixture.id = :fixtureId group by p.pick")
    List<Object[]> countByPick(@Param("fixtureId") Long fixtureId);

    @EntityGraph(attributePaths = "member")
    List<Prediction> findByFixtureIdAndResult(Long fixtureId, PredictionResult result);
}
