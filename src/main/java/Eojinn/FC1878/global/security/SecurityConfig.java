package Eojinn.FC1878.global.security;

import Eojinn.FC1878.oauth.OAuthLoginHandlers;
import Eojinn.FC1878.oauth.RedisAuthorizationRequestRepository;
import Eojinn.FC1878.oauth.SocialClientRegistrationRepository;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SocialClientRegistrationRepository clientRegistrationRepository;
    private final RedisAuthorizationRequestRepository authorizationRequestRepository;
    private final OAuthLoginHandlers oAuthLoginHandlers;

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 에러 응답(/error)은 누구나 받을 수 있게. 막혀 있으면 404·500 이 전부 401 로 바뀌어 보임
                        .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/auth/**", "/actuator/health", "/ws/**",
                                "/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/leagues/**", "/api/teams/**", "/api/fixtures/**", "/api/rankings/**", "/api/posts/**", "/api/overseas/**", "/api/transfers/**", "/api/news/**", "/api/search/**", "/api/search", "/api/players/**", "/api/career", "/api/chat/status").permitAll()
                        // AI 챗봇: 손님도 질문 가능 (하루 질문 수는 ChatRateLimiter 가 제한)
                        .requestMatchers(HttpMethod.POST, "/api/chat").permitAll()
                        .anyRequest().authenticated())
                // 소셜 로그인: /oauth2/authorization/{google|kakao|apple} 로 시작 → /login/oauth2/code/{...} 로 돌아옴
                .oauth2Login(o -> o
                        .clientRegistrationRepository(clientRegistrationRepository)
                        .authorizationEndpoint(a -> a.authorizationRequestRepository(authorizationRequestRepository))
                        .successHandler(oAuthLoginHandlers.success())
                        .failureHandler(oAuthLoginHandlers.failure()))
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
