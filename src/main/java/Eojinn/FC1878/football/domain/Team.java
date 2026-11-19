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

    private String logoUrl;

    public static Team of(Integer externalId, String name, String logoUrl) {
        Team t = new Team();
        t.externalId = externalId;
        t.update(name, logoUrl);
        return t;
    }

    public void update(String name, String logoUrl) {
        this.name = name;
        this.logoUrl = logoUrl;
    }
}
