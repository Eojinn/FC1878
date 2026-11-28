package Eojinn.FC1878.football.domain;

import Eojinn.FC1878.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer externalId;

    @Column(nullable = false, length = 100)
    private String name;

    /** 화면 표시용 한글 이름 (없으면 name 사용) */
    @Column(length = 100)
    private String koreanName;

    private String logoUrl;

    /** 검색용 한글 이름·별명 ("맨체스터 유나이티드 | 맨유 | …"). 화면 표시에는 안 씀 */
    @Column(length = 500)
    private String searchNames;

    /** 최근 5경기 승무패 "WDLWW" (모든 대회, 왼쪽이 오래된 경기) */
    @Column(length = 10)
    private String recentForm;

    public static Team of(Integer externalId, String name, String logoUrl) {
        Team t = new Team();
        t.externalId = externalId;
        t.update(name, logoUrl);
        return t;
    }

    public void changeKoreanName(String koreanName) {
        this.koreanName = koreanName;
    }

    public void changeRecentForm(String recentForm) {
        this.recentForm = recentForm;
    }

    public void changeSearchNames(String searchNames) {
        this.searchNames = searchNames;
    }

    public String getDisplayName() {
        return koreanName != null && !koreanName.isBlank() ? koreanName : name;
    }

    public void update(String name, String logoUrl) {
        this.name = name;
        this.logoUrl = logoUrl;
    }
}
