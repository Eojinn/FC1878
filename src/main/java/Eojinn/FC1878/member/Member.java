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

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean isWithdrawn() {
        return role == Role.WITHDRAWN;
    }

    /**
     * 회원 탈퇴: 게시글·댓글·예측 기록은 남겨두고 개인정보만 지운다.
     * 이메일이 바뀌므로 같은 이메일로 다시 가입할 수 있고, 예전 비밀번호로는 로그인할 수 없다.
     */
    public void withdraw(String randomEncodedPassword) {
        this.email = "withdrawn-" + id + "@deleted.fc1878";
        this.nickname = "탈퇴한회원" + id;
        this.password = randomEncodedPassword;
        this.role = Role.WITHDRAWN;
        this.point = 0;
        this.favoriteTeam = null;
    }

    public void changeFavoriteTeam(Team team) {
        this.favoriteTeam = team;
    }
}
