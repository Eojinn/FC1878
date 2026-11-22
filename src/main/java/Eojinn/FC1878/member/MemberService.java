package Eojinn.FC1878.member;

import Eojinn.FC1878.football.domain.Team;
import Eojinn.FC1878.football.repository.TeamRepository;
import Eojinn.FC1878.global.error.BusinessException;
import Eojinn.FC1878.global.error.ErrorCode;
import Eojinn.FC1878.member.MemberDtos.MemberResponse;
import lombok.RequiredArgsConstructor;
import Eojinn.FC1878.oauth.SocialAccount;
import Eojinn.FC1878.oauth.SocialAccountRepository;
import Eojinn.FC1878.ranking.RankingService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;

    private final PasswordEncoder passwordEncoder;
    private final RankingService rankingService;
    private final RefreshTokenService refreshTokenService;
    private final SocialAccountRepository socialAccountRepository;

    private MemberResponse toResponse(Member member) {
        var providers = socialAccountRepository.findByMemberId(member.getId()).stream()
                .map(SocialAccount::getProvider).distinct().sorted().toList();
        return MemberResponse.from(member, providers);
    }

    /** 탈퇴한 회원은 없는 회원으로 취급 (탈퇴 전에 받은 로그인 토큰으로도 아무것도 못 하게) */
    public Member getMember(Long id) {
        return memberRepository.findById(id)
                .filter(m -> !m.isWithdrawn())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public MemberResponse me(Long memberId) {
        return toResponse(getMember(memberId));
    }

    @Transactional
    public MemberResponse changeNickname(Long memberId, String nickname) {
        Member member = getMember(memberId);
        String trimmed = MemberDtos.normalizeNickname(nickname);
        if (isReservedNickname(trimmed)) throw new BusinessException(ErrorCode.RESERVED_NICKNAME);
        if (!trimmed.equals(member.getNickname()) && memberRepository.existsByNickname(trimmed)) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }
        member.changeNickname(trimmed);
        try {
            memberRepository.flush(); // 동시에 같은 닉네임으로 바꾸는 경우를 여기서 잡음
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(MemberConstraint.toErrorCode(e));
        }
        return toResponse(member);
    }

    @Transactional
    /** 비밀번호를 바꾸면 지금 쓰는 기기(currentRefreshToken)만 남기고 다른 기기는 모두 로그아웃 */
    public void changePassword(Long memberId, String currentPassword, String newPassword, String currentRefreshToken) {
        Member member = getMember(memberId);
        if (!member.isPasswordLogin()) throw new BusinessException(ErrorCode.SOCIAL_ONLY_ACCOUNT);
        checkPassword(member, currentPassword);
        if (passwordEncoder.matches(newPassword, member.getPassword())) {
            throw new BusinessException(ErrorCode.SAME_PASSWORD);
        }
        PasswordPolicy.personalInfoViolation(newPassword, member.getEmail(), member.getNickname()).ifPresent(reason -> {
            throw new BusinessException(ErrorCode.WEAK_PASSWORD, reason);
        });
        member.changePassword(passwordEncoder.encode(newPassword));
        refreshTokenService.revokeAll(memberId, currentRefreshToken);
    }

    @Transactional
    public void withdraw(Long memberId, String password) {
        Member member = getMember(memberId);
        // 소셜로만 가입한 회원은 비밀번호가 없으므로 확인 생략 (화면에서 동의 체크로 대신함)
        if (member.isPasswordLogin()) checkPassword(member, password);
        socialAccountRepository.deleteByMemberId(memberId); // 소셜 연결도 끊어서, 다시 로그인하면 새 계정으로
        member.withdraw(passwordEncoder.encode(UUID.randomUUID().toString()));
        rankingService.remove(memberId);
        refreshTokenService.revokeAll(memberId, null);
    }

    /** 탈퇴 회원 표시용 이름과 겹치지 않게 */
    public static boolean isReservedNickname(String nickname) {
        return nickname.startsWith("탈퇴한회원");
    }

    private void checkPassword(Member member, String rawPassword) {
        if (rawPassword == null || !passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }
    }

    @Transactional
    public MemberResponse changeFavoriteTeam(Long memberId, Long teamId) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));
        Member member = getMember(memberId);
        member.changeFavoriteTeam(team);
        return toResponse(member);
    }
}
