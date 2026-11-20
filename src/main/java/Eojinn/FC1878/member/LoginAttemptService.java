package Eojinn.FC1878.member;

import Eojinn.FC1878.global.error.BusinessException;
import Eojinn.FC1878.global.error.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Locale;

/**
 * 로그인 시도 제한 (무차별 대입 공격 방지). Redis 에 실패 횟수를 센다.
 * - 같은 이메일로 maxAttempts 번 틀리면 그 이메일은 lockMinutes 분 동안 로그인 금지
 * - 같은 IP 에서 ipMaxAttempts 번 틀리면(이메일을 바꿔가며 시도하는 경우) 그 IP 를 lockMinutes 분 동안 금지
 * 실패 횟수는 마지막 실패 후 lockMinutes 분이 지나면 저절로 초기화되고, 로그인에 성공하면 바로 초기화된다.
 * 이메일이 실제로 가입돼 있는지와 관계없이 똑같이 세기 때문에, 이 기능으로 가입 여부를 알아낼 수는 없다.
 */
@Service
public class LoginAttemptService {

    private static final String EMAIL_FAIL = "auth:login-fail:email:";
    private static final String IP_FAIL = "auth:login-fail:ip:";

    private final StringRedisTemplate redis;
    private final int maxAttempts;
    private final int ipMaxAttempts;
    private final Duration lockTime;

    public LoginAttemptService(StringRedisTemplate redis,
                               @Value("${app.auth.login-max-attempts:5}") int maxAttempts,
                               @Value("${app.auth.login-ip-max-attempts:30}") int ipMaxAttempts,
                               @Value("${app.auth.login-lock-minutes:10}") int lockMinutes) {
        this.redis = redis;
        this.maxAttempts = maxAttempts;
        this.ipMaxAttempts = ipMaxAttempts;
        this.lockTime = Duration.ofMinutes(lockMinutes);
    }

    /** 로그인 전에 호출: 잠겨 있으면 429 에러 */
    public void checkAllowed(String email, String ip) {
        long emailLeft = lockedSeconds(EMAIL_FAIL + key(email), maxAttempts);
        long ipLeft = lockedSeconds(IP_FAIL + ip, ipMaxAttempts);
        long left = Math.max(emailLeft, ipLeft);
        if (left > 0) {
            long minutes = Math.max(1, (left + 59) / 60);
            throw new BusinessException(ErrorCode.LOGIN_LOCKED,
                    "로그인 시도가 너무 많아요. " + minutes + "분 후에 다시 시도해 주세요.");
        }
    }

    /** 로그인 실패 시 호출. 남은 시도 횟수를 담은 에러를 돌려줌 */
    public BusinessException recordFailure(String email, String ip) {
        long count = increment(EMAIL_FAIL + key(email));
        increment(IP_FAIL + ip);
        long remaining = maxAttempts - count;
        if (remaining <= 0) {
            return new BusinessException(ErrorCode.LOGIN_LOCKED,
                    maxAttempts + "번 틀려서 " + lockTime.toMinutes() + "분 동안 로그인이 제한돼요.");
        }
        if (remaining <= 2) {
            return new BusinessException(ErrorCode.LOGIN_FAILED,
                    ErrorCode.LOGIN_FAILED.getMessage() + " (" + remaining + "번 더 틀리면 "
                            + lockTime.toMinutes() + "분 동안 로그인이 제한돼요)");
        }
        return new BusinessException(ErrorCode.LOGIN_FAILED);
    }

    /** 로그인 성공 시 호출: 이 이메일의 실패 기록 초기화 */
    public void reset(String email) {
        redis.delete(EMAIL_FAIL + key(email));
    }

    private long increment(String key) {
        Long count = redis.opsForValue().increment(key);
        redis.expire(key, lockTime); // 실패할 때마다 잠금 시간을 다시 잼
        return count == null ? 0 : count;
    }

    /** 한도를 넘었으면 남은 잠금 시간(초), 아니면 0 */
    private long lockedSeconds(String key, int limit) {
        String v = redis.opsForValue().get(key);
        if (v == null || Long.parseLong(v) < limit) return 0;
        Long ttl = redis.getExpire(key);
        return ttl == null || ttl < 0 ? lockTime.toSeconds() : ttl;
    }

    private static String key(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
