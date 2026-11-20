package Eojinn.FC1878.oauth;

import java.util.Map;

/**
 * 소셜 서비스마다 다른 사용자 정보 모양을 하나로 맞춤.
 *
 * @param emailVerified 소셜 서비스가 "본인 확인된 이메일"이라고 보증하는지 (기존 회원과 자동 연결할 때만 사용)
 */
public record SocialUserInfo(String provider, String providerUserId, String email, boolean emailVerified,
                             String nickname) {

    @SuppressWarnings("unchecked")
    public static SocialUserInfo of(String provider, Map<String, Object> attrs) {
        return switch (provider) {
            // 구글 (OpenID Connect): sub, email, email_verified, name
            case "google" -> new SocialUserInfo(provider, str(attrs.get("sub")), str(attrs.get("email")),
                    bool(attrs.get("email_verified")), str(attrs.get("name")));
            // 카카오: { id, kakao_account: { email, is_email_verified, profile: { nickname } }, properties: { nickname } }
            case "kakao" -> {
                Map<String, Object> account = (Map<String, Object>) attrs.getOrDefault("kakao_account", Map.of());
                Map<String, Object> profile = (Map<String, Object>) account.getOrDefault("profile", Map.of());
                Map<String, Object> properties = (Map<String, Object>) attrs.getOrDefault("properties", Map.of());
                String nickname = str(profile.get("nickname"));
                if (nickname == null) nickname = str(properties.get("nickname"));
                yield new SocialUserInfo(provider, str(attrs.get("id")), str(account.get("email")),
                        bool(account.get("is_email_verified")) && bool(account.get("is_email_valid"), true), nickname);
            }
            // 애플 (OpenID Connect): sub, email, email_verified. 이름은 첫 로그인 때만 따로 와서 사용하지 않음
            case "apple" -> new SocialUserInfo(provider, str(attrs.get("sub")), str(attrs.get("email")),
                    bool(attrs.get("email_verified")), null);
            default -> throw new IllegalArgumentException("지원하지 않는 소셜 로그인: " + provider);
        };
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static boolean bool(Object o) {
        return bool(o, false);
    }

    /** true / "true" 둘 다 처리 (애플은 문자열로 줄 때가 있음) */
    private static boolean bool(Object o, boolean defaultValue) {
        if (o == null) return defaultValue;
        if (o instanceof Boolean b) return b;
        return "true".equalsIgnoreCase(String.valueOf(o));
    }
}
