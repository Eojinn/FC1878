package Eojinn.FC1878.global.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secret, long accessTokenValiditySeconds, long refreshTokenValidityDays) {
}
