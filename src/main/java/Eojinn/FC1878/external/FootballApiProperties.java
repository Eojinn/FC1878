package Eojinn.FC1878.external;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * football-data.org 연동 설정 (application.yaml 의 football.api.*)
 *
 * @param competitions 동기화할 대회 코드 (PL, PD, SA, BL1, FL1, CL …)
 * @param season       순위 조회 시 기본 시즌 (시즌 시작 연도, 2026-27 시즌이면 2026)
 */
@ConfigurationProperties(prefix = "football.api")
public record FootballApiProperties(boolean enabled, String baseUrl, String key,
                                    Integer season, List<String> competitions) {

    /** 토큰은 영문·숫자만. 예시 문구나 한글이 들어가 있으면 서버 시작 때 바로 알려 줌 */
    public FootballApiProperties {
        if (key != null && !key.isBlank() && !key.trim().matches("[A-Za-z0-9]+")) {
            throw new IllegalStateException("football.api.key 값이 올바르지 않아요. "
                    + "config/application.yaml 의 key: 에 football-data.org 토큰(영문·숫자)을 넣었는지 확인해 주세요. "
                    + "토큰이 없으면 key: 줄을 지우거나 # 으로 주석 처리하면 샘플 데이터로 실행돼요.");
        }
        if (key != null) key = key.trim();
    }
}
