package Eojinn.FC1878.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 소셜 로그인 설정 (application.yaml 의 app.oauth.*). 값이 비어 있는 서비스는 꺼진 상태로 취급.
 *
 * @param frontendUrl 로그인 후 돌아갈 프론트 주소
 */
@ConfigurationProperties(prefix = "app.oauth")
public record OAuthProperties(String frontendUrl, Google google, Kakao kakao, Apple apple) {

    public record Google(String clientId, String clientSecret) {
        public boolean enabled() {
            return notBlank(clientId) && notBlank(clientSecret);
        }
    }

    /** scope: 기본 profile_nickname. 이메일 동의 항목을 설정했다면 "profile_nickname,account_email" */
    public record Kakao(String clientId, String clientSecret, String scope) {
        public boolean enabled() {
            return notBlank(clientId) && notBlank(clientSecret);
        }
    }

    /**
     * @param clientId       Services ID (예: com.example.fc1878.login)
     * @param teamId         Apple Developer 팀 ID
     * @param keyId          Sign in with Apple 키 ID
     * @param privateKey     .p8 파일 내용 (privateKeyPath 와 둘 중 하나)
     * @param privateKeyPath .p8 파일 경로
     */
    public record Apple(String clientId, String teamId, String keyId, String privateKey, String privateKeyPath) {
        public boolean enabled() {
            return notBlank(clientId) && notBlank(teamId) && notBlank(keyId)
                    && (notBlank(privateKey) || notBlank(privateKeyPath));
        }
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
