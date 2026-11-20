package Eojinn.FC1878.member;

import Eojinn.FC1878.global.error.BusinessException;
import Eojinn.FC1878.global.error.ErrorCode;
import Eojinn.FC1878.global.security.JwtProperties;
import Eojinn.FC1878.global.security.JwtProvider;
import Eojinn.FC1878.member.MemberDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;

    /** 로그인/갱신 결과: 본문으로 줄 액세스 토큰 + 쿠키로 줄 리프레시 토큰 */
    public record AuthResult(TokenResponse body, RefreshTokenService.Issued refresh) {
    }

    @Transactional
    public Long signup(SignupRequest req) {
        if (memberRepository.existsByEmail(req.email())) throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        if (MemberService.isReservedNickname(req.nickname())) throw new BusinessException(ErrorCode.RESERVED_NICKNAME);
        if (memberRepository.existsByNickname(req.nickname())) throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        Member member = Member.create(req.email(), passwordEncoder.encode(req.password()), req.nickname());
        return memberRepository.save(member).getId();
    }

    @Transactional(readOnly = true)
    public AuthResult login(LoginRequest req) {
        Member member = memberRepository.findByEmail(req.email())
                .filter(m -> !m.isWithdrawn())
                .filter(m -> passwordEncoder.matches(req.password(), m.getPassword()))
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
        return new AuthResult(accessToken(member), refreshTokenService.issue(member.getId(), req.remember()));
    }

    /** 쿠키의 리프레시 토큰으로 새 액세스 토큰 발급 (리프레시 토큰도 새로 교체) */
    @Transactional(readOnly = true)
    public AuthResult refresh(String refreshToken) {
        RefreshTokenService.Issued issued = refreshTokenService.rotate(refreshToken);
        Member member = memberRepository.findById(issued.memberId())
                .filter(m -> !m.isWithdrawn())
                .orElseThrow(() -> {
                    refreshTokenService.revoke(issued.token());
                    return new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
                });
        return new AuthResult(accessToken(member), issued);
    }

    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    private TokenResponse accessToken(Member member) {
        return TokenResponse.bearer(jwtProvider.createAccessToken(member.getId(), member.getRole().name()),
                jwtProperties.accessTokenValiditySeconds());
    }
}
