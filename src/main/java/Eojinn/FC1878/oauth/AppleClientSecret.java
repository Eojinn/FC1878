package Eojinn.FC1878.oauth;

import io.jsonwebtoken.Jwts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

/**
 * 애플은 client secret 을 고정 문자열이 아니라 "개인 키(.p8)로 서명한 JWT"로 받는다. 최대 유효기간 6개월.
 * https://developer.apple.com/documentation/accountorganizationaldatasharing/creating-a-client-secret
 */
final class AppleClientSecret {

    /** 애플 최대치(6개월)보다 짧게 */
    static final Duration VALIDITY = Duration.ofDays(150);

    private AppleClientSecret() {
    }

    static String create(OAuthProperties.Apple apple, Instant now) {
        return Jwts.builder()
                .header().keyId(apple.keyId()).and()
                .issuer(apple.teamId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(VALIDITY)))
                .audience().add("https://appleid.apple.com").and()
                .subject(apple.clientId())
                .signWith(loadKey(apple), Jwts.SIG.ES256)
                .compact();
    }

    private static PrivateKey loadKey(OAuthProperties.Apple apple) {
        try {
            String pem = apple.privateKey() != null && !apple.privateKey().isBlank()
                    ? apple.privateKey().replace("\\n", "\n")
                    : Files.readString(Path.of(apple.privateKeyPath()));
            String base64 = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            return KeyFactory.getInstance("EC").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64)));
        } catch (IOException e) {
            throw new IllegalStateException("애플 개인 키 파일을 읽을 수 없어요: " + apple.privateKeyPath(), e);
        } catch (Exception e) {
            throw new IllegalStateException("애플 개인 키(.p8) 형식이 올바르지 않아요", e);
        }
    }
}
