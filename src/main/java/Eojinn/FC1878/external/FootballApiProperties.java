package Eojinn.FC1878.external;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "football.api")
public record FootballApiProperties(boolean enabled, String baseUrl, String key,
                                    Integer season, List<Integer> leagueIds) {
}
