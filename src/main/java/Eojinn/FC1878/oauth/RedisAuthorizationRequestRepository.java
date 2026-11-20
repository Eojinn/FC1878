package Eojinn.FC1878.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.io.*;
import java.time.Duration;
import java.util.Base64;

/**
 * 소셜 로그인 도중의 요청 정보를 세션 대신 Redis 에 state 값으로 저장한다.
 * - 이 서버는 세션을 쓰지 않는(STATELESS) 구조라서
 * - 애플은 결과를 다른 사이트에서 POST 로 보내는데, 그때는 브라우저가 세션 쿠키를 안 보내서 세션 방식이 실패함
 */
@Component
@RequiredArgsConstructor
public class RedisAuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String KEY = "oauth2:auth-request:";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redis;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        String state = request.getParameter("state");
        if (state == null) return null;
        return deserialize(redis.opsForValue().get(KEY + state));
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) return;
        redis.opsForValue().set(KEY + authorizationRequest.getState(), serialize(authorizationRequest), TTL);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        String state = request.getParameter("state");
        if (state == null) return null;
        return deserialize(redis.opsForValue().getAndDelete(KEY + state));
    }

    private static String serialize(OAuth2AuthorizationRequest req) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(req);
            out.flush();
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Redis 에는 이 서버가 쓴 값만 들어있음 (사용자가 보낸 값을 역직렬화하지 않음) */
    private static OAuth2AuthorizationRequest deserialize(String value) {
        if (value == null) return null;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(value)))) {
            return (OAuth2AuthorizationRequest) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }
}
