package Eojinn.FC1878.football.domain;

import Eojinn.FC1878.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

/** 경기 (MySQL 에서 MATCH 는 예약어라 fixture 로 명명) */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Fixture extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer externalId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "league_id")
    private League league;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "home_team_id")
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "away_team_id")
    private Team awayTeam;

    @Column(nullable = false)
    private Integer season;

    private String round;

    /** KST 기준 킥오프 시각 */
    @Column(nullable = false)
    private LocalDateTime kickoffAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private FixtureStatus status;

    private Integer elapsed;
    private Integer homeScore;
    private Integer awayScore;
    private Integer homePenalty;
    private Integer awayPenalty;

    /** 승부예측 포인트 정산 완료 여부 */
    @Column(nullable = false)
    private boolean settled;

    public static Fixture of(Integer externalId, League league, Team home, Team away, Integer season) {
        Fixture f = new Fixture();
        f.externalId = externalId;
        f.league = league;
        f.homeTeam = home;
        f.awayTeam = away;
        f.season = season;
        f.status = FixtureStatus.SCHEDULED;
        return f;
    }

    /** @return 스코어/상태가 바뀌었으면 true (실시간 푸시 판단용) */
    public boolean updateState(String round, LocalDateTime kickoffAt, FixtureStatus status, Integer elapsed,
                               Integer homeScore, Integer awayScore, Integer homePenalty, Integer awayPenalty) {
        boolean changed = this.status != status
                || !Objects.equals(this.homeScore, homeScore)
                || !Objects.equals(this.awayScore, awayScore)
                || !Objects.equals(this.elapsed, elapsed);
        this.round = round;
        this.kickoffAt = kickoffAt;
        this.status = status;
        this.elapsed = elapsed;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.homePenalty = homePenalty;
        this.awayPenalty = awayPenalty;
        return changed;
    }

    public boolean isPredictable(LocalDateTime now) {
        return status == FixtureStatus.SCHEDULED && now.isBefore(kickoffAt);
    }

    public void markSettled() {
        this.settled = true;
    }
}
