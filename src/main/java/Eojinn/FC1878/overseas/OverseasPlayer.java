package Eojinn.FC1878.overseas;

import Eojinn.FC1878.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 해외 리그에서 뛰는 한국 선수 한 명 (소속팀·리그·지도 좌표 포함) */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OverseasPlayer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;

    /** 영문 이름 (예: Son Heung-min) */
    @Column(length = 60)
    private String nameEn;

    /** 다른 영문 표기, 쉼표로 구분 (예: Baek Seung-ho) */
    @Column(length = 200)
    private String nameAliases;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 2)
    private Position position;

    @Column(nullable = false, length = 60)
    private String club;

    /** 임대 선수면 원소속팀, 아니면 null */
    @Column(length = 60)
    private String parentClub;

    @Column(nullable = false, length = 60)
    private String league;

    /** ISO 국가 코드. 영국은 잉글랜드 GB-ENG, 스코틀랜드 GB-SCT 처럼 나눠서 씀 */
    @Column(nullable = false, length = 10)
    private String countryCode;

    @Column(nullable = false, length = 30)
    private String countryName;

    /** 연고지(지도 마커 위치) */
    @Column(nullable = false, length = 40)
    private String city;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    /** 목록 정렬용 (작을수록 위) */
    @Column(nullable = false)
    private Integer sortOrder;

    public boolean isOnLoan() {
        return parentClub != null && !parentClub.isBlank();
    }

    public enum Position { GK, DF, MF, FW }
}
