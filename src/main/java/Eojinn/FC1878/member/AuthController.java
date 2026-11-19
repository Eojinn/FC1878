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

    /** "로그인하고 좋아하는 팀 팔로우" 기능 */
    @PutMapping("/api/members/me/favorite-team")
    public MemberResponse favoriteTeam(@AuthenticationPrincipal Long memberId,
                                       @Valid @RequestBody FavoriteTeamRequest req) {
        return memberService.changeFavoriteTeam(memberId, req.teamId());
    }
}
