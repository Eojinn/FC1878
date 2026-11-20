package Eojinn.FC1878.member;

import Eojinn.FC1878.global.error.BusinessException;
import Eojinn.FC1878.global.error.ErrorCode;
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

    @Transactional
    public Long signup(SignupRequest req) {
        if (memberRepository.existsByEmail(req.email())) throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        if (MemberService.isReservedNickname(req.nickname())) throw new BusinessException(ErrorCode.RESERVED_NICKNAME);
        if (memberRepository.existsByNickname(req.nickname())) throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        Member member = Member.create(req.email(), passwordEncoder.encode(req.password()), req.nickname());
        return memberRepository.save(member).getId();
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest req) {
        Member member = memberRepository.findByEmail(req.email())
                .filter(m -> passwordEncoder.matches(req.password(), m.getPassword()))
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
        return TokenResponse.bearer(jwtProvider.createAccessToken(member.getId(), member.getRole().name()));
    }
}
