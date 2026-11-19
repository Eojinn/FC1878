package Eojinn.FC1878.football.domain;

import Eojinn.FC1878.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class League extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 외부 API(API-Football)의 리그 ID */
    @Column(nullable = false, unique = true)
    private Integer externalId;

    @Column(nullable = false, length = 100)
    private String name;

    /** 화면 표시용 한글 이름 (없으면 name 사용) */
    @Column(length = 100)
    private String koreanName;

    private String country;
    private String logoUrl;

    public static League of(Integer externalId, String name, String country, String logoUrl) {
        League l = new League();
        l.externalId = externalId;
        l.update(name, country, logoUrl);
        return l;
    }

    public void changeKoreanName(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getDisplayName() {
        return koreanName != null && !koreanName.isBlank() ? koreanName : name;
    }

    public void update(String name, String country, String logoUrl) {
        this.name = name;
        this.country = country;
        this.logoUrl = logoUrl;
    }
}
