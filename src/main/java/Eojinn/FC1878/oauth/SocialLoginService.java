package Eojinn.FC1878.oauth;

import Eojinn.FC1878.member.Member;
import Eojinn.FC1878.member.MemberDtos;
import Eojinn.FC1878.member.MemberRepository;
import Eojinn.FC1878.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 소셜 로그인 결과로 우리 회원을 찾거나 만든다.
 * 1) 이미 연결된 소셜 계정이면 → 그 회원
 * 2) 소셜 서비스가 확인해 준 이메일이 기존 회원 이메일과 같으면 → 그 회원에 연결 (이메일로 가입했던 사람이 구글로 로그인하는 경우)
 * 3) 아니면 → 새 회원 가입 (비밀번호 없이 소셜로만 로그인하는 계정)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SocialLoginService {

    private static final Map<String, String> PROVIDER_KO = Map.of("google", "구글", "kakao", "카카오", "apple", "애플");

    private final SocialAccountRepository socialAccountRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Member login(SocialUserInfo info) {
        if (info.providerUserId() == null) throw new IllegalStateException("소셜 계정 ID를 받지 못했어요");

        // 1) 이미 연결된 계정
        var linked = socialAccountRepository.findByProviderAndProviderUserId(info.provider(), info.providerUserId());
        if (linked.isPresent()) {
            Member member = linked.get().getMember();
            if (member.isWithdrawn()) throw new IllegalStateException("탈퇴한 계정이에요");
            return member;
        }

        String email = MemberDtos.normalizeEmail(info.email());

        // 2) 확인된 이메일이 같은 기존 회원에 연결
        if (email != null && info.emailVerified()) {
            var existing = memberRepository.findByEmail(email).filter(m -> !m.isWithdrawn());
            if (existing.isPresent()) {
                socialAccountRepository.save(SocialAccount.of(existing.get(), info.provider(), info.providerUserId(), email));
                log.info("소셜 계정 연결: member={} provider={}", existing.get().getId(), info.provider());
                return existing.get();
            }
        }

        // 3) 새 회원
        String memberEmail = email != null && !memberRepository.existsByEmail(email)
                ? email
                : (info.provider() + "_" + info.providerUserId() + "@social.fc1878").toLowerCase();
        Member member = Member.createSocial(memberEmail, passwordEncoder.encode(UUID.randomUUID().toString()),
                uniqueNickname(info));
        memberRepository.save(member);
        socialAccountRepository.save(SocialAccount.of(member, info.provider(), info.providerUserId(), email));
        log.info("소셜 회원 가입: member={} provider={}", member.getId(), info.provider());
        return member;
    }

    /** 소셜 닉네임을 쓰되, 겹치거나 쓸 수 없으면 뒤에 숫자를 붙임 */
    private String uniqueNickname(SocialUserInfo info) {
        String base = MemberDtos.normalizeNickname(info.nickname());
        if (base == null || base.length() < 2 || MemberService.isReservedNickname(base)) {
            base = PROVIDER_KO.getOrDefault(info.provider(), "소셜") + "회원";
        }
        if (base.length() > 15) base = base.substring(0, 15).strip();
        if (!memberRepository.existsByNickname(base)) return base;
        for (int i = 0; i < 20; i++) {
            String candidate = base + ThreadLocalRandom.current().nextInt(1000, 10000);
            if (!memberRepository.existsByNickname(candidate)) return candidate;
        }
        return base + UUID.randomUUID().toString().substring(0, 4);
    }
}
