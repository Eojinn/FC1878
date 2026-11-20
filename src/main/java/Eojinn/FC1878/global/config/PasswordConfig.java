package Eojinn.FC1878.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 비밀번호 암호화기. SecurityConfig 안에 두면
 * SecurityConfig → 소셜 로그인 처리기 → 회원 서비스 → PasswordEncoder → SecurityConfig 로 순환 참조가 생겨서 분리함
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
