package Eojinn.FC1878.member;

import Eojinn.FC1878.global.error.BusinessException;
import Eojinn.FC1878.global.error.ErrorCode;
import Eojinn.FC1878.global.security.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Set;

/**
 * 리프레시 토큰(로그인 유지용)을 Redis 에 저장·검증한다.
 * - 토큰 원문은 쿠키로만 주고, Redis 에는 SHA-256 해시만 저장 (Redis 가 털려도 토큰을 못 씀)
 * - 사용할 때마다 새 토큰으로 바꿔줌(rotation). 여러 탭이 동시에 갱신해도 안 끊기게 옛 토큰은 20초간만 더 허용
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String KEY = "auth:refresh:";            // auth:refresh:{hash} -> "memberId:rememberMe"
    private static final String MEMBER_KEY = "auth:refresh:member:"; // auth:refresh:member:{id} -> {hash...}
    private static final Duration ROTATION_GRACE = Duration.ofSeconds(20);
    /** "로그인 상태 유지"를 체크하지 않았을 때: 브라우저를 닫으면 쿠키가 사라지고, 서버에서도 하루 뒤 만료 */
    private static final Duration SESSION_TTL = Duration.ofDays(1);

    private final StringRedisTemplate redis;
    private final JwtProperties props;
    private final SecureRandom random = new SecureRandom();

    public record Issued(Long memberId, String token, boolean rememberMe, Duration ttl) {
    }

    public Issued issue(Long memberId, boolean rememberMe) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String hash = hash(token);
        Duration ttl = ttl(rememberMe);

        redis.opsForValue().set(KEY + hash, memberId + ":" + (rememberMe ? 1 : 0), ttl);
        redis.opsForSet().add(MEMBER_KEY + memberId, hash);
        redis.expire(MEMBER_KEY + memberId, rememberLongest());
        return new Issued(memberId, token, rememberMe, ttl);
    }

    /** 리프레시 토큰을 확인하고 새 토큰으로 교체 */
    public Issued rotate(String token) {
        if (token == null || token.isBlank()) throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        String key = KEY + hash(token);
        String value = redis.opsForValue().get(key);
        if (value == null) throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);

        String[] parts = value.split(":");
        Long memberId = Long.valueOf(parts[0]);
        boolean rememberMe = parts.length > 1 && "1".equals(parts[1]);

        Long remaining = redis.getExpire(key);
        if (remaining == null || remaining > ROTATION_GRACE.toSeconds()) {
            redis.expire(key, ROTATION_GRACE);
        }
        return issue(memberId, rememberMe);
    }

    /** 이 토큰 하나만 폐기 (로그아웃) */
    public void revoke(String token) {
        if (token == null || token.isBlank()) return;
        String hash = hash(token);
        String value = redis.opsForValue().get(KEY + hash);
        redis.delete(KEY + hash);
        if (value != null) redis.opsForSet().remove(MEMBER_KEY + value.split(":")[0], hash);
    }

    /** 이 회원의 모든 기기 로그아웃. keepToken 이 있으면 그 토큰(지금 쓰는 기기)은 남김 */
    public void revokeAll(Long memberId, String keepToken) {
        String keepHash = keepToken == null || keepToken.isBlank() ? null : hash(keepToken);
        Set<String> hashes = redis.opsForSet().members(MEMBER_KEY + memberId);
        if (hashes == null) return;
        for (String h : hashes) {
            if (h.equals(keepHash)) continue;
            redis.delete(KEY + h);
            redis.opsForSet().remove(MEMBER_KEY + memberId, h);
        }
    }

    private Duration ttl(boolean rememberMe) {
        return rememberMe ? rememberLongest() : SESSION_TTL;
    }

    private Duration rememberLongest() {
        return Duration.ofDays(props.refreshTokenValidityDays());
    }

    private static String hash(String token) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
