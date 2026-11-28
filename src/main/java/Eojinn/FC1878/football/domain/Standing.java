package Eojinn.FC1878.football.domain;

import Eojinn.FC1878.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_standing", columnNames = {"league_id", "season", "team_id"}))
public class Standing extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "league_id")
    private League league;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(nullable = false)
    private Integer season;

    @Column(name = "team_rank", nullable = false)
    private int rank;

    private int played;
    private int win;
    private int draw;
    private int lose;
    private int goalsFor;
    private int goalsAgainst;
    private int points;
    private String form;

    public static Standing of(League league, Team team, Integer season) {
        Standing s = new Standing();
        s.league = league;
        s.team = team;
        s.season = season;
        return s;
    }

    public void update(int rank, int played, int win, int draw, int lose,
                       int goalsFor, int goalsAgainst, int points, String form) {
        this.rank = rank;
        this.played = played;
        this.win = win;
        this.draw = draw;
        this.lose = lose;
        this.goalsFor = goalsFor;
        this.goalsAgainst = goalsAgainst;
        this.points = points;
        this.form = form;
    }

    /** 리그 최근 5경기 (직접 계산한 값으로 덮어씀) */
    public void changeForm(String form) {
        this.form = form;
    }

    public int getGoalDiff() {
        return goalsFor - goalsAgainst;
    }
}
