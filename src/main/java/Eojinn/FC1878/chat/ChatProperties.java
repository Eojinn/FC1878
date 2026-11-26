package Eojinn.FC1878.chat;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 축구 챗봇 설정 (application.yaml 의 app.ai).
 * Google Gemini API 를 씀. 키는 Google AI Studio 에서 무료로 받을 수 있음 (무료 등급은 하루·분당 호출 수 제한).
 * 키가 비어 있으면 챗봇 창에 "연결 전" 안내만 보여줌.
 *
 * @param guestDailyLimit  로그인 안 한 사용자(IP 기준) 하루 질문 수
 * @param memberDailyLimit 회원 1명 하루 질문 수
 * @param maxToolRounds    한 질문에서 AI 가 DB 를 조회할 수 있는 최대 횟수
 */
@ConfigurationProperties("app.ai")
public record ChatProperties(String baseUrl, String apiKey, String model,
                             Integer guestDailyLimit, Integer memberDailyLimit, Integer maxToolRounds) {

    public ChatProperties {
        if (baseUrl == null || baseUrl.isBlank()) baseUrl = "https://generativelanguage.googleapis.com/v1beta";
        if (model == null || model.isBlank()) model = "gemini-flash-latest";
        if (guestDailyLimit == null || guestDailyLimit < 0) guestDailyLimit = 10;
        if (memberDailyLimit == null || memberDailyLimit < 0) memberDailyLimit = 50;
        if (maxToolRounds == null || maxToolRounds < 1) maxToolRounds = 5;
        if (apiKey != null) apiKey = apiKey.trim();
        if (apiKey != null && !apiKey.isEmpty() && !apiKey.matches("[\\x21-\\x7E]+")) {
            throw new IllegalStateException("app.ai.api-key 값이 올바르지 않아요. "
                    + "한글·띄어쓰기 없이 Google AI Studio 에서 받은 API 키만 환경변수 GEMINI_API_KEY 나 "
                    + "config/application.yaml 에 넣었는지 확인해 주세요. 없으면 비워 두면 돼요.");
        }
    }

    public boolean configured() {
        return apiKey != null && !apiKey.isEmpty();
    }
}
