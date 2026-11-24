package Eojinn.FC1878.news;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 뉴스 설정 (application.yaml 의 app.news).
 * 네이버 API 허브 키가 비어 있으면 샘플 기사를 보여줌.
 */
@ConfigurationProperties("app.news")
public record NewsProperties(String naverBaseUrl, String naverClientId, String naverClientSecret,
                             Integer cacheMinutes) {

    public NewsProperties {
        if (naverBaseUrl == null || naverBaseUrl.isBlank()) naverBaseUrl = "https://naverapihub.apigw.ntruss.com";
        if (cacheMinutes == null || cacheMinutes < 1) cacheMinutes = 10;
    }

    public boolean configured() {
        return naverClientId != null && !naverClientId.isBlank()
                && naverClientSecret != null && !naverClientSecret.isBlank();
    }
}
