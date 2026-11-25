package Eojinn.FC1878.chat;

import Eojinn.FC1878.global.common.TimeUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** 하루 질문 수 제한 (한국 시간 자정에 초기화). 무료 AI 한도를 한 사람이 다 쓰지 않게 */
@Component
public class ChatRateLimiter {

    private final ChatProperties props;
    private final Map<String, AtomicInteger> counts = new ConcurrentHashMap<>();
    private volatile LocalDate day = LocalDate.now(TimeUtil.KST);

    public ChatRateLimiter(ChatProperties props) {
        this.props = props;
    }

    public int limit(Long memberId) {
        return memberId != null ? props.memberDailyLimit() : props.guestDailyLimit();
    }

    public int remaining(Long memberId, String ip) {
        roll();
        AtomicInteger c = counts.get(key(memberId, ip));
        return Math.max(0, limit(memberId) - (c == null ? 0 : c.get()));
    }

    /** 한 번 쓸 수 있으면 세고 true */
    public boolean tryAcquire(Long memberId, String ip) {
        roll();
        AtomicInteger c = counts.computeIfAbsent(key(memberId, ip), k -> new AtomicInteger());
        if (c.incrementAndGet() > limit(memberId)) {
            c.decrementAndGet();
            return false;
        }
        return true;
    }

    /** AI 쪽 오류로 답을 못 줬으면 돌려줌 */
    public void refund(Long memberId, String ip) {
        AtomicInteger c = counts.get(key(memberId, ip));
        if (c != null && c.get() > 0) c.decrementAndGet();
    }

    private String key(Long memberId, String ip) {
        return memberId != null ? "m:" + memberId : "ip:" + ip;
    }

    private void roll() {
        LocalDate today = LocalDate.now(TimeUtil.KST);
        if (!today.equals(day)) {
            synchronized (this) {
                if (!today.equals(day)) {
                    counts.clear();
                    day = today;
                }
            }
        }
    }
}
