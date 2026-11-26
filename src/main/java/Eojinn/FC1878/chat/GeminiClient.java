package Eojinn.FC1878.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Google Gemini generateContent 호출 (POST /models/{model}:generateContent, 헤더 x-goog-api-key).
 * 요청·응답은 Map 그대로 다룸: 모델이 보낸 내용(thoughtSignature 포함)을 다음 요청에 그대로 돌려줘야 해서.
 * 응답은 먼저 글자(String)로 받은 뒤 JSON 으로 바꿈: 응답 형식(Content-Type)이 예상과 달라도 내용을 읽고,
 * 못 읽으면 무엇이 왔는지 로그로 보여 주려고.
 */
@Slf4j
@Component
public class GeminiClient {

    private final RestClient restClient;
    private final ChatProperties props;
    private final ObjectMapper objectMapper;

    public GeminiClient(ChatProperties props, ObjectMapper objectMapper) {
        this.props = props;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(60));
        this.restClient = RestClient.builder()
                .baseUrl(props.baseUrl())
                .requestFactory(factory)
                .defaultHeader("x-goog-api-key", props.configured() ? props.apiKey() : "")
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /** 응답 + 실제로 답한 모델 (한 질문 안에서는 같은 모델을 계속 써야 해서) */
    public record Reply(Map<String, Object> body, String model) {
    }

    /**
     * fixedModel 이 있으면 그 모델만, 없으면 기본 모델 → 예비 모델 순서로 시도.
     * 붐빔(503·429·500)은 같은 모델로 한 번 더 시도한 뒤 다음 모델로, 없는 모델(404)은 바로 다음 모델로.
     */
    public Reply generate(Map<String, Object> body, String fixedModel) {
        Set<String> models = new LinkedHashSet<>();
        if (fixedModel != null) {
            models.add(fixedModel);
        } else {
            models.add(props.model());
            props.fallbackModels().stream().filter(m -> m != null && !m.isBlank()).map(String::trim).forEach(models::add);
        }
        GeminiException last = null;
        for (String model : models) {
            for (int attempt = 1; attempt <= 2; attempt++) {
                try {
                    return new Reply(call(model, body), model);
                } catch (GeminiException e) {
                    last = e;
                    if (e.status() == 404) break;                 // 없는 모델 → 다음 모델
                    if (!e.busy()) throw e;                       // 키 오류 등은 다른 모델로 바꿔도 같음
                    if (attempt == 1) sleep(1500);                // 잠깐 쉬고 같은 모델로 한 번 더
                }
            }
            if (fixedModel == null) log.info("Gemini 모델 '{}' 대신 다음 모델로 시도해요.", model);
        }
        throw last != null ? last : new GeminiException(0);
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> call(String model, Map<String, Object> body) {
        ResponseEntity<String> res;
        try {
            res = restClient.post()
                    .uri("/models/{model}:generateContent", model)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toEntity(String.class);
        } catch (RestClientResponseException e) {
            int status = e.getStatusCode().value();
            String detail = e.getResponseBodyAsString();
            if (status == 404) {
                log.warn("Gemini 모델 '{}' 을(를) 찾지 못했어요. app.ai.model 을 AI Studio 에 있는 모델 이름으로 바꿔 주세요. {}",
                        model, detail);
            } else {
                log.warn("Gemini 오류 ({}): HTTP {} {}", model, status, detail.replaceAll("\\s+", " "));
            }
            throw new GeminiException(status);
        } catch (RestClientException e) {
            log.warn("Gemini 연결 실패: {}", e.getMessage());
            throw new GeminiException(0);
        }

        String text = res.getBody();
        if (text == null || text.isBlank()) {
            log.warn("Gemini 응답이 비어 있어요: HTTP {}, Content-Type {}", res.getStatusCode().value(),
                    res.getHeaders().getContentType());
            throw new GeminiException(0);
        }
        try {
            return objectMapper.readValue(text, Map.class);
        } catch (RuntimeException e) {
            log.warn("Gemini 응답을 JSON 으로 읽지 못했어요: HTTP {}, Content-Type {}, 내용 앞부분: {}",
                    res.getStatusCode().value(), res.getHeaders().getContentType(),
                    text.substring(0, Math.min(300, text.length())).replaceAll("\\s+", " "));
            throw new GeminiException(0);
        }
    }

    /** status 0 = 연결 실패·응답 이상, 429 = 사용량 초과, 503 = 모델이 붐빔 */
    public static class GeminiException extends RuntimeException {
        private final int status;

        public GeminiException(int status) {
            super("Gemini 호출 실패 (HTTP " + status + ")");
            this.status = status;
        }

        public int status() {
            return status;
        }

        /** 잠깐 기다리거나 다른 모델로 바꾸면 될 수 있는 오류 */
        public boolean busy() {
            return status == 429 || status == 500 || status == 503;
        }
    }
}
