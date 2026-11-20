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
}
