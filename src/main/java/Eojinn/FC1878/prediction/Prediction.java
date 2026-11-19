package Eojinn.FC1878.prediction;

import Eojinn.FC1878.football.domain.Fixture;
import Eojinn.FC1878.global.common.BaseTimeEntity;
import Eojinn.FC1878.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_prediction", columnNames = {"member_id", "fixture_id"}))
public class Prediction extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fixture_id")
    private Fixture fixture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(10)")
    private Pick pick;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(10)")
    private PredictionResult result;

    public static Prediction of(Member member, Fixture fixture, Pick pick) {
        Prediction p = new Prediction();
        p.member = member;
        p.fixture = fixture;
        p.pick = pick;
        p.result = PredictionResult.PENDING;
        return p;
    }

    public void changePick(Pick pick) {
        this.pick = pick;
    }

    /** @return 적중 여부 */
    public boolean settle(Pick actual) {
        this.result = (this.pick == actual) ? PredictionResult.WIN : PredictionResult.LOSE;
        return this.result == PredictionResult.WIN;
    }
}
