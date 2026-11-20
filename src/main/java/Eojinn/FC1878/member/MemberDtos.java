package Eojinn.FC1878.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class MemberDtos {
    private MemberDtos() {
    }

    /**
     * 닉네임 정리: 앞뒤 공백 제거 + 중간의 연속 공백은 한 칸으로.
     * " 골든부트 ", "골든  부트" 같은 값이 다른 닉네임으로 따로 가입되는 것을 막음
     */
    /**
     * 이메일 정리: 앞뒤 공백 제거 + 소문자. "Test@A.com" 과 "test@a.com" 을 같은 계정으로 취급.
     * (MySQL 기본 설정은 대소문자를 구분하지 않지만, DB 설정에 기대지 않고 저장·비교하는 값을 통일)
     */
    public static String normalizeEmail(String email) {
        return email == null ? null : email.strip().toLowerCase(java.util.Locale.ROOT);
    }

    public static String normalizeNickname(String nickname) {
        return nickname == null ? null : nickname.strip().replaceAll("(?U)\\s+", " ");
    }

    public record SignupRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 64) String password,
            @NotBlank @Size(min = 2, max = 20) String nickname) {
        /** 요청을 받자마자 정리 → 형식·길이 검사도 정리된 값으로 함 */
        public SignupRequest {
            email = normalizeEmail(email);
            nickname = normalizeNickname(nickname);
        }
    }

    /** rememberMe: "로그인 상태 유지" 체크 여부 (생략하면 false) */
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password, Boolean rememberMe) {
        public LoginRequest {
            email = normalizeEmail(email);
        }

        public boolean remember() {
            return Boolean.TRUE.equals(rememberMe);
        }
    }

    /** expiresIn: 액세스 토큰 유효 시간(초) */
    public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
        public static TokenResponse bearer(String token, long expiresIn) {
            return new TokenResponse(token, "Bearer", expiresIn);
        }
    }

    public record FavoriteTeamRequest(@NotNull Long teamId) {
    }

    public record ChangeNicknameRequest(@NotBlank @Size(min = 2, max = 20) String nickname) {
        public ChangeNicknameRequest {
            nickname = normalizeNickname(nickname);
        }
    }

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 8, max = 64) String newPassword) {
    }

    /** password: 이메일 가입 회원은 필수, 소셜 가입 회원은 생략 */
    public record WithdrawRequest(String password) {
    }

    /**
     * @param passwordLogin   이메일+비밀번호 로그인 가능 여부 (false 면 비밀번호 변경 화면을 숨김)
     * @param socialProviders 연결된 소셜 로그인 (google, kakao, apple)
     */
    public record MemberResponse(Long id, String email, String nickname, int point,
                                 Long favoriteTeamId, String favoriteTeamName, String favoriteTeamLogo,
                                 boolean passwordLogin, java.util.List<String> socialProviders) {
        public static MemberResponse from(Member m, java.util.List<String> socialProviders) {
            var t = m.getFavoriteTeam();
            // 소셜 가입 시 이메일을 못 받은 경우 임시 주소 대신 빈 값으로 보여줌
            String email = m.getEmail().endsWith("@social.fc1878") ? null : m.getEmail();
            return new MemberResponse(m.getId(), email, m.getNickname(), m.getPoint(),
                    t == null ? null : t.getId(), t == null ? null : t.getDisplayName(),
                    t == null ? null : t.getLogoUrl(), m.isPasswordLogin(), socialProviders);
        }
    }
}
