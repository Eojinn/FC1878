package Eojinn.FC1878.oauth;

import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 키가 설정된 소셜 로그인만 등록한다. (Spring Boot 기본 설정은 키가 비어 있으면 서버가 아예 안 켜지므로 직접 구성)
 * 애플은 client secret(JWT)이 만료되기 전에 자동으로 새로 만든다.
 */
@Component
public class SocialClientRegistrationRepository implements ClientRegistrationRepository {

    private static final String REDIRECT_URI = "{baseUrl}/login/oauth2/code/{registrationId}";

    private final OAuthProperties props;
    private volatile ClientRegistration apple;
    private volatile Instant appleSecretCreatedAt;

    public SocialClientRegistrationRepository(OAuthProperties props) {
        this.props = props;
    }

    /** 사용 가능한 소셜 로그인 이름 (로그인 화면에 버튼으로 보여줄 것) */
    public List<String> enabledProviders() {
        List<String> list = new ArrayList<>();
        if (props.google() != null && props.google().enabled()) list.add("google");
        if (props.kakao() != null && props.kakao().enabled()) list.add("kakao");
        if (props.apple() != null && props.apple().enabled()) list.add("apple");
        return list;
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        if (!enabledProviders().contains(registrationId)) return null;
        return switch (registrationId) {
            case "google" -> google();
            case "kakao" -> kakao();
            case "apple" -> apple();
            default -> null;
        };
    }

    private ClientRegistration google() {
        return CommonOAuth2Provider.GOOGLE.getBuilder("google")
                .clientId(props.google().clientId())
                .clientSecret(props.google().clientSecret())
                .scope("openid", "profile", "email")
                .build();
    }

    private ClientRegistration kakao() {
        String scope = props.kakao().scope() == null || props.kakao().scope().isBlank()
                ? "profile_nickname" : props.kakao().scope();
        return ClientRegistration.withRegistrationId("kakao")
                .clientName("Kakao")
                .clientId(props.kakao().clientId())
                .clientSecret(props.kakao().clientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(REDIRECT_URI)
                .scope(Arrays.stream(scope.split(",")).map(String::trim).toList())
                .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                .tokenUri("https://kauth.kakao.com/oauth/token")
                .userInfoUri("https://kapi.kakao.com/v2/user/me")
                .userNameAttributeName("id")
                .build();
    }

    private ClientRegistration apple() {
        Instant now = Instant.now();
        if (apple == null || appleSecretCreatedAt.plus(AppleClientSecret.VALIDITY.minusDays(7)).isBefore(now)) {
            synchronized (this) {
                if (apple == null || appleSecretCreatedAt.plus(AppleClientSecret.VALIDITY.minusDays(7)).isBefore(now)) {
                    var a = props.apple();
                    apple = ClientRegistration.withRegistrationId("apple")
                            .clientName("Apple")
                            .clientId(a.clientId())
                            .clientSecret(AppleClientSecret.create(a, now))
                            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                            .redirectUri(REDIRECT_URI)
                            .scope("openid", "email", "name")
                            // 이름·이메일을 요청하면 애플은 결과를 POST(form_post)로 보내야 함
                            .authorizationUri("https://appleid.apple.com/auth/authorize?response_mode=form_post")
                            .tokenUri("https://appleid.apple.com/auth/token")
                            .jwkSetUri("https://appleid.apple.com/auth/keys")
                            .userNameAttributeName(IdTokenClaimNames.SUB)
                            .build();
                    appleSecretCreatedAt = now;
                }
            }
        }
        return apple;
    }
}
