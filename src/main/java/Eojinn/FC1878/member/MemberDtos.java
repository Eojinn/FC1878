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
    public static String normalizeNickname(String nickname) {
        return nickname == null ? null : nickname.strip().replaceAll("(?U)\\s+", " ");
    }

    public record SignupRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 64) String password,
            @NotBlank @Size(min = 2, max = 20) String nickname) {
        /** 요청을 받자마자 정리 → 길이 검사(@Size)도 정리된 값으로 함 */
        public SignupRequest {
            nickname = normalizeNickname(nickname);
        }
    }

    /** rememberMe: "로그인 상태 유지" 체크 여부 (생략하면 false) */
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password, Boolean rememberMe) {
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

    public record WithdrawRequest(@NotBlank String password) {
    }

    public record MemberResponse(Long id, String email, String nickname, int point,
                                 Long favoriteTeamId, String favoriteTeamName, String favoriteTeamLogo) {
        public static MemberResponse from(Member m) {
            var t = m.getFavoriteTeam();
            return new MemberResponse(m.getId(), m.getEmail(), m.getNickname(), m.getPoint(),
                    t == null ? null : t.getId(), t == null ? null : t.getName(), t == null ? null : t.getLogoUrl());
        }
    }
}
