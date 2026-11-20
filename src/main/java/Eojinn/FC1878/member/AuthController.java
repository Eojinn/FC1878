package Eojinn.FC1878.member;

import Eojinn.FC1878.member.MemberDtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;

    @PostMapping("/api/auth/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> signup(@Valid @RequestBody SignupRequest req) {
        return Map.of("id", authService.signup(req));
    }

    @PostMapping("/api/auth/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
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
                               @Valid @RequestBody ChangePasswordRequest req) {
        memberService.changePassword(memberId, req.currentPassword(), req.newPassword());
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
