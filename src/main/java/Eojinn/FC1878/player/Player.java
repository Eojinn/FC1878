package Eojinn.FC1878.player;

import Eojinn.FC1878.football.domain.Team;
import Eojinn.FC1878.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 선수 한 명. football-data.org 스쿼드 동기화로 채워짐 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Player extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer externalId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 40)
    private String position;

    /** GK / DF / MF / FW */
    @Column(nullable = false, length = 2)
    private String role;

    private Integer shirtNumber;

    @Column(length = 60)
    private String nationality;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    /** 팀이 아직 DB에 없을 때도 보여줄 수 있게 이름·엠블럼을 같이 저장 */
    @Column(length = 100)
    private String teamName;

    private String teamLogoUrl;

    /** 마지막으로 스쿼드에서 확인된 시각 (오래 안 보이면 삭제) */
    @Column(nullable = false)
    private LocalDateTime syncedAt;

    public static Player create(Integer externalId) {
        Player p = new Player();
        p.externalId = externalId;
        return p;
    }

    public void update(String name, String position, Integer shirtNumber, String nationality,
                       Team team, String teamName, String teamLogoUrl, LocalDateTime syncedAt) {
        this.name = name;
        this.position = position;
        this.role = PlayerRole.of(position);
        this.shirtNumber = shirtNumber;
        this.nationality = nationality;
        this.team = team;
        this.teamName = team != null ? team.getDisplayName() : teamName;
        this.teamLogoUrl = team != null && team.getLogoUrl() != null ? team.getLogoUrl() : teamLogoUrl;
        this.syncedAt = syncedAt;
    }
}
