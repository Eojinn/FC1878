package Eojinn.FC1878.oauth;

import Eojinn.FC1878.global.common.BaseTimeEntity;
import Eojinn.FC1878.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 회원 ↔ 소셜 계정(구글·카카오·애플) 연결. 한 회원이 여러 소셜 계정을 연결할 수 있음 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_social_account", columnNames = {"provider", "provider_user_id"}))
public class SocialAccount extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    /** google, kakao, apple */
    @Column(nullable = false, length = 20)
    private String provider;

    /** 소셜 서비스가 주는 고유 번호 (구글·애플 sub, 카카오 id) */
    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    @Column(length = 100)
    private String email;

    public static SocialAccount of(Member member, String provider, String providerUserId, String email) {
        SocialAccount s = new SocialAccount();
        s.member = member;
        s.provider = provider;
        s.providerUserId = providerUserId;
        s.email = email;
        return s;
    }
}
