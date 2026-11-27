package Eojinn.FC1878.player;

import Eojinn.FC1878.football.domain.Team;
import Eojinn.FC1878.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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

    private LocalDate dateOfBirth;

    /** 한글 이름 (Wikidata 에서 찾아서 채움, 못 찾으면 null) */
    @Column(length = 60)
    private String koreanName;

    private LocalDateTime koreanNameCheckedAt;

    /** 등번호를 Wikidata 에서 찾아본 시각 (football-data 무료 데이터엔 등번호가 거의 없음) */
    private LocalDateTime shirtNumberCheckedAt;

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

    /** 한글 이름 조회 결과 기록 (못 찾았으면 koreanName 은 그대로 두고 시각만) */
    public void markKoreanName(String koreanName, LocalDateTime checkedAt) {
        if (koreanName != null && !koreanName.isBlank()) this.koreanName = koreanName.trim();
        this.koreanNameCheckedAt = checkedAt;
    }

    /** 등번호 조회 결과 기록 (못 찾았으면 번호는 그대로 두고 시각만) */
    public void markShirtNumber(Integer number, LocalDateTime checkedAt) {
        if (number != null && number > 0 && number < 100) this.shirtNumber = number;
        this.shirtNumberCheckedAt = checkedAt;
    }

    /** "2001-09-05" → 날짜 (형식이 다르면 null) */
    public static LocalDate parseDate(String s) {
        try {
            return s == null || s.length() < 10 ? null : LocalDate.parse(s.substring(0, 10));
        } catch (java.time.format.DateTimeParseException e) {
            return null;
        }
    }

    public static Player create(Integer externalId) {
        Player p = new Player();
        p.externalId = externalId;
        return p;
    }

    public void update(String name, String position, Integer shirtNumber, String nationality, LocalDate dateOfBirth,
                       Team team, String teamName, String teamLogoUrl, LocalDateTime syncedAt) {
        this.name = name;
        if (dateOfBirth != null) this.dateOfBirth = dateOfBirth;
        this.position = position;
        this.role = PlayerRole.of(position);
        if (shirtNumber != null || this.shirtNumber == null) this.shirtNumber = shirtNumber;
        this.nationality = nationality;
        this.team = team;
        this.teamName = team != null ? team.getDisplayName() : teamName;
        this.teamLogoUrl = team != null && team.getLogoUrl() != null ? team.getLogoUrl() : teamLogoUrl;
        this.syncedAt = syncedAt;
    }
}
