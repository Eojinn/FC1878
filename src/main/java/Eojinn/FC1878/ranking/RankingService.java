package Eojinn.FC1878.ranking;

import Eojinn.FC1878.member.Member;
import Eojinn.FC1878.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Redis Sorted Set 기반 포인트 랭킹 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    private static final String KEY = "ranking:points";

    private final StringRedisTemplate redis;
    private final MemberRepository memberRepository;

    public record RankingEntry(long rank, Long memberId, String nickname, long point) {
    }

    public void addScore(Long memberId, int delta) {
        redis.opsForZSet().incrementScore(KEY, String.valueOf(memberId), delta);
    }

    @Transactional(readOnly = true)
    public List<RankingEntry> top(int size) {
        Set<TypedTuple<String>> tuples = redis.opsForZSet().reverseRangeWithScores(KEY, 0, size - 1);
        if (tuples == null || tuples.isEmpty()) return List.of();

        List<Long> ids = tuples.stream().map(t -> Long.valueOf(Objects.requireNonNull(t.getValue()))).toList();
        Map<Long, Member> members = memberRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));

        List<RankingEntry> result = new ArrayList<>();
        long rank = 1;
        for (TypedTuple<String> t : tuples) {
            Long id = Long.valueOf(Objects.requireNonNull(t.getValue()));
            Member m = members.get(id);
            long score = t.getScore() == null ? 0 : t.getScore().longValue();
            result.add(new RankingEntry(rank++, id, m == null ? "(탈퇴회원)" : m.getNickname(), score));
        }
        return result;
    }

    public Optional<Long> myRank(Long memberId) {
        Long r = redis.opsForZSet().reverseRank(KEY, String.valueOf(memberId));
        return Optional.ofNullable(r).map(v -> v + 1);
    }

    /** 서버 시작 시 DB 포인트로 Redis 랭킹 재구성 (Redis 가 비었을 때 대비) */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void rebuild() {
        try {
            redis.delete(KEY);
            for (Member m : memberRepository.findByPointGreaterThan(0)) {
                redis.opsForZSet().add(KEY, String.valueOf(m.getId()), m.getPoint());
            }
        } catch (Exception e) {
            log.warn("랭킹 재구성 실패 (Redis 연결 확인): {}", e.getMessage());
        }
    }
}
