package Eojinn.FC1878.member;

import Eojinn.FC1878.member.MemberDtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import Eojinn.FC1878.global.error.BusinessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;
    private final RefreshCookie refreshCookie;

    @PostMapping("/api/auth/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> signup(@Valid @RequestBody SignupRequest req) {
        return Map.of("id", authService.signup(req));
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
        return withCookie(authService.login(req));
    }

    /** 액세스 토큰이 만료되면 프론트가 자동으로 호출 (리프레시 토큰은 HttpOnly 쿠키로 옴) */
    @PostMapping("/api/auth/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @CookieValue(name = RefreshCookie.NAME, required = false) String refreshToken) {
        try {
            return withCookie(authService.refresh(refreshToken));
        } catch (BusinessException e) {
            // 쓸 수 없는 쿠키는 지워서, 다음부터 헛된 갱신 요청을 안 보내게
            return ResponseEntity.status(e.getErrorCode().getStatus())
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.clear().toString())
                    .build();
        }
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = RefreshCookie.NAME, required = false) String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.clear().toString())
                .build();
    }

    private ResponseEntity<TokenResponse> withCookie(AuthService.AuthResult result) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.create(result.refresh()).toString())
                .body(result.body());
    }

    @GetMapping("/api/members/me")
    public MemberResponse me(@AuthenticationPrincipal Long memberId) {
        return memberService.me(memberId);
    }

    /** 닉네임 변경 */
    @PatchMapping("/api/members/me/nickname")
    public MemberResponse changeNickname(@AuthenticationPrincipal Long memberId,
                                         @Valid @RequestBody ChangeNicknameRequest req) {
        return memberService.changeNickname(memberId, req.nickname());
    }

    /** 비밀번호 변경 (현재 비밀번호 확인) */
    @PatchMapping("/api/members/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal Long memberId,
                               @Valid @RequestBody ChangePasswordRequest req,
                               @CookieValue(name = RefreshCookie.NAME, required = false) String refreshToken) {
        memberService.changePassword(memberId, req.currentPassword(), req.newPassword(), refreshToken);
    }

    /** 회원 탈퇴 (비밀번호 확인). DELETE 본문을 버리는 환경이 있어 POST 로 받음 */
    @PostMapping("/api/members/me/withdraw")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(@AuthenticationPrincipal Long memberId, @Valid @RequestBody WithdrawRequest req) {
        memberService.withdraw(memberId, req.password());
    }

    /** "로그인하고 좋아하는 팀 팔로우" 기능 */
    @PutMapping("/api/members/me/favorite-team")
    public MemberResponse favoriteTeam(@AuthenticationPrincipal Long memberId,
                                       @Valid @RequestBody FavoriteTeamRequest req) {
        return memberService.changeFavoriteTeam(memberId, req.teamId());
    }
}
