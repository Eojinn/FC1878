package Eojinn.FC1878.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OAuthController {

    private final SocialClientRegistrationRepository registrations;

    /** 로그인 화면에 보여줄 소셜 로그인 버튼 목록 (키가 설정된 것만) */
    @GetMapping("/api/auth/providers")
    public List<String> providers() {
        return registrations.enabledProviders();
    }
}
