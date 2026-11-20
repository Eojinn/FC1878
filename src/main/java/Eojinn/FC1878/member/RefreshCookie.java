package Eojinn.FC1878.member;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * 리프레시 토큰 쿠키. HttpOnly 라서 자바스크립트(XSS)로 읽을 수 없고, /api 요청에만 붙는다.
 * (비밀번호 변경 때 "지금 기기"를 알아보려고 /api/auth 가 아니라 /api 로 둠)
 */
@Component
public class RefreshCookie {

    public static final String NAME = "fc1878_refresh";
    private static final String PATH = "/api";

    /** 배포(https)에서는 true 로 */
    @Value("${app.auth.cookie-secure:false}")
    private boolean secure;

    public ResponseCookie create(RefreshTokenService.Issued issued) {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(NAME, issued.token())
                .httpOnly(true).secure(secure).sameSite("Lax").path(PATH);
        // 로그인 유지: 기간이 있는 쿠키 / 아니면 브라우저를 닫으면 사라지는 세션 쿠키
        if (issued.rememberMe()) b.maxAge(issued.ttl());
        return b.build();
    }

    public ResponseCookie clear() {
        return ResponseCookie.from(NAME, "").httpOnly(true).secure(secure).sameSite("Lax").path(PATH).maxAge(0).build();
    }
}
