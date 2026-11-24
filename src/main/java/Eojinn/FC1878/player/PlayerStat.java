package Eojinn.FC1878.player;

import Eojinn.FC1878.football.domain.League;
import Eojinn.FC1878.football.domain.Team;
import Eojinn.FC1878.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 선수의 대회·시즌별 기록 (골·도움·페널티·출전 경기).
 * 무료 플랜은 대회 득점 순위에 든 선수의 기록만 주기 때문에, 순위 밖 선수는 기록이 없음.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerStat extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "league_id")
    private League league;

    @Column(nullable = false)
    private Integer season;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    private Integer playedMatches;

    @Column(nullable = false)
    private int goals;

    private Integer assists;

    private Integer penalties;

    public static PlayerStat create(Player player, League league, Integer season) {
        PlayerStat s = new PlayerStat();
        s.player = player;
        s.league = league;
        s.season = season;
        return s;
    }

    public void update(Team team, Integer playedMatches, Integer goals, Integer assists, Integer penalties) {
        this.team = team;
        this.playedMatches = playedMatches;
        this.goals = goals == null ? 0 : goals;
        this.assists = assists;
        this.penalties = penalties;
    }
}
