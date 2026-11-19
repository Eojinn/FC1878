package Eojinn.FC1878.member;

import Eojinn.FC1878.football.domain.Team;
import Eojinn.FC1878.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 30)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private Role role;

    @Column(nullable = false)
    private int point;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "favorite_team_id")
    private Team favoriteTeam;

    public static Member create(String email, String encodedPassword, String nickname) {
        Member m = new Member();
        m.email = email;
        m.password = encodedPassword;
        m.nickname = nickname;
        m.role = Role.USER;
        m.point = 0;
        return m;
    }

    public void addPoint(int amount) {
        this.point += amount;
    }

    public void changeFavoriteTeam(Team team) {
        this.favoriteTeam = team;
    }
}
