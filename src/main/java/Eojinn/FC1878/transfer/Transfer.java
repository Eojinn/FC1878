package Eojinn.FC1878.transfer;

import Eojinn.FC1878.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 확정된 이적 한 건 (구단 공식 발표 기준) */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transfer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 화면 표시용 한글 이름 */
    @Column(nullable = false, length = 40)
    private String playerName;

    @Column(length = 60)
    private String playerNameEn;

    /** 국적 (한글). 모르면 null */
    @Column(length = 30)
    private String nationality;

    /** 한국 선수 여부 ("한국 선수만" 필터용) */
    @Column(nullable = false)
    private boolean korean;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 2)
    private Position position;

    @Column(nullable = false, length = 60)
    private String fromClub;

    @Column(length = 40)
    private String fromLeague;

    @Column(nullable = false, length = 60)
    private String toClub;

    @Column(length = 40)
    private String toLeague;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private TransferType type;

    /** 기본 이적료 (백만 유로). 비공개·자유 이적이면 null */
    @Column(precision = 7, scale = 2)
    private BigDecimal feeMillionEur;

    /** 옵션·완전 이적 조항 같은 보충 설명 */
    @Column(length = 80)
    private String feeNote;

    /** 공식 발표일 */
    @Column(nullable = false)
    private LocalDate transferDate;

    /** 계약 만료일 (임대면 임대 종료일). 모르면 null */
    private LocalDate contractUntil;

    public enum Position { GK, DF, MF, FW }

    public enum TransferType {
        PERMANENT,   // 완전 이적
        LOAN,        // 임대
        FREE,        // 자유 이적 (이적료 없음)
        LOAN_RETURN  // 임대 복귀
    }
}
