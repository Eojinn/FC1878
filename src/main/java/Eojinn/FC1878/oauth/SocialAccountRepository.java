package Eojinn.FC1878.oauth;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    @EntityGraph(attributePaths = "member")
    Optional<SocialAccount> findByProviderAndProviderUserId(String provider, String providerUserId);

    List<SocialAccount> findByMemberId(Long memberId);

    void deleteByMemberId(Long memberId);
}
