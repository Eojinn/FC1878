package Eojinn.FC1878.oauth;

import Eojinn.FC1878.member.Member;
import Eojinn.FC1878.member.RefreshCookie;
import Eojinn.FC1878.member.RefreshTokenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 소셜 로그인 성공 → 로그인 유지 쿠키(리프레시 토큰)를 주고 프론트의 /oauth/callback 으로 보냄.
 * 프론트는 그 쿠키로 액세스 토큰을 받아감 (토큰을 주소창에 노출하지 않음).
 * 실패 → 프론트 로그인 화면으로 오류 메시지와 함께 보냄.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthLoginHandlers {

    private final SocialLoginService socialLoginService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshCookie refreshCookie;
    private final OAuthProperties props;

    public AuthenticationSuccessHandler success() {
        return (request, response, authentication) -> {
            try {
                OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
                SocialUserInfo info = SocialUserInfo.of(token.getAuthorizedClientRegistrationId(),
                        token.getPrincipal().getAttributes());
                Member member = socialLoginService.login(info);
                var issued = refreshTokenService.issue(member.getId(), true);
                response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.create(issued).toString());
                response.sendRedirect(props.frontendUrl() + "/oauth/callback");
            } catch (Exception e) {
                log.warn("소셜 로그인 처리 실패", e);
                redirectError(response, "소셜 로그인 처리 중 문제가 생겼어요. " + e.getMessage());
            }
        };
    }

    public AuthenticationFailureHandler failure() {
        return (request, response, exception) -> {
            log.warn("소셜 로그인 실패: {}", exception.getMessage());
            redirectError(response, "소셜 로그인에 실패했어요. 다시 시도해 주세요.");
        };
    }

    private void redirectError(HttpServletResponse response, String message) throws java.io.IOException {
        response.sendRedirect(props.frontendUrl() + "/login?error="
                + URLEncoder.encode(message, StandardCharsets.UTF_8));
    }
}
