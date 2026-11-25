package Eojinn.FC1878.chat;

import Eojinn.FC1878.chat.ChatDtos.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * AI 축구 챗봇. 질문 → Gemini 가 필요한 도구(DB 조회)를 고름 → 결과를 돌려주면 → Gemini 가 한국어로 답함.
 * 한 질문에서 도구는 최대 maxToolRounds 번까지.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private static final int MAX_HISTORY = 10;
    private static final int MAX_LINKS = 6;

    private final GeminiClient gemini;
    private final ChatTools tools;
    private final ChatProperties props;
    private final ChatRateLimiter limiter;

    public ChatStatus status(Long memberId, String ip) {
        int limit = limiter.limit(memberId);
        return new ChatStatus(props.configured(), limiter.remaining(memberId, ip), limit, memberId != null);
    }

    public ChatResponse chat(List<ChatMessage> messages, Long memberId, String ip) {
        if (!props.configured()) {
            return new ChatResponse("NOT_CONFIGURED",
                    "AI 챗봇이 아직 연결되지 않았어요. 관리자가 Gemini API 키를 넣으면 쓸 수 있어요.", List.of(), null);
        }
        if (messages.isEmpty() || !"user".equals(messages.get(messages.size() - 1).role())) {
            return new ChatResponse("ERROR", "질문을 입력해 주세요.", List.of(), null);
        }
        if (!limiter.tryAcquire(memberId, ip)) {
            String more = memberId == null ? " 로그인하면 하루 " + props.memberDailyLimit() + "번까지 물어볼 수 있어요." : "";
            return new ChatResponse("LIMIT", "오늘 질문 수를 다 썼어요. 내일 다시 물어봐 주세요." + more, List.of(), 0);
        }

        try {
            Set<ChatLink> links = new LinkedHashSet<>();
            String answer = run(messages, links);
            return new ChatResponse("OK", answer, links.stream().limit(MAX_LINKS).toList(), limiter.remaining(memberId, ip));
        } catch (GeminiClient.GeminiException e) {
            limiter.refund(memberId, ip);
            String msg = e.status() == 429
                    ? "지금 AI 사용량이 많아서 답을 못 했어요. 1분쯤 뒤에 다시 물어봐 주세요."
                    : "AI 답변을 받지 못했어요. 잠시 후 다시 시도해 주세요.";
            return new ChatResponse(e.status() == 429 ? "BUSY" : "ERROR", msg, List.of(), limiter.remaining(memberId, ip));
        }
    }

    @SuppressWarnings("unchecked")
    private String run(List<ChatMessage> messages, Set<ChatLink> links) {
        List<Map<String, Object>> contents = new ArrayList<>();
        int from = Math.max(0, messages.size() - MAX_HISTORY);
        for (ChatMessage m : messages.subList(from, messages.size())) {
            contents.add(Map.of("role", "user".equals(m.role()) ? "user" : "model",
                    "parts", List.of(Map.of("text", m.text().strip()))));
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt()))));
        body.put("tools", List.of(Map.of("functionDeclarations", tools.declarations())));
        body.put("generationConfig", Map.of("temperature", 0.3, "maxOutputTokens", 2048));
        body.put("contents", contents);

        for (int round = 0; round <= props.maxToolRounds(); round++) {
            Map<String, Object> res = gemini.generate(body);
            Map<String, Object> content = firstContent(res);
            if (content == null) return blockedMessage(res);

            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.getOrDefault("parts", List.of());
            List<Map<String, Object>> calls = parts.stream()
                    .filter(p -> p.get("functionCall") instanceof Map)
                    .map(p -> (Map<String, Object>) p.get("functionCall"))
                    .toList();

            if (calls.isEmpty() || round == props.maxToolRounds()) {
                String text = parts.stream()
                        .filter(p -> p.get("text") instanceof String && !Boolean.TRUE.equals(p.get("thought")))
                        .map(p -> (String) p.get("text"))
                        .reduce("", String::concat).strip();
                return text.isEmpty() ? "답을 찾지 못했어요. 질문을 조금 바꿔서 다시 물어봐 주세요." : text;
            }

            // 모델이 보낸 내용을 그대로(thoughtSignature 포함) 넣고, 도구 결과를 이어 붙임
            contents.add(content);
            List<Map<String, Object>> responses = new ArrayList<>();
            for (Map<String, Object> call : calls) {
                String name = String.valueOf(call.get("name"));
                Map<String, Object> args = call.get("args") instanceof Map<?, ?> a ? (Map<String, Object>) a : Map.of();
                log.debug("챗봇 도구 호출: {} {}", name, args);
                Map<String, Object> fr = new LinkedHashMap<>();
                fr.put("name", name);
                if (call.get("id") != null) fr.put("id", call.get("id"));
                fr.put("response", tools.execute(name, args, links));
                responses.add(Map.of("functionResponse", fr));
            }
            contents.add(Map.of("role", "user", "parts", responses));
        }
        return "답을 찾지 못했어요.";
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> firstContent(Map<String, Object> res) {
        if (res == null || !(res.get("candidates") instanceof List<?> cands) || cands.isEmpty()) return null;
        Object c = ((Map<String, Object>) cands.get(0)).get("content");
        return c instanceof Map<?, ?> m ? (Map<String, Object>) m : null;
    }

    private static String blockedMessage(Map<String, Object> res) {
        log.debug("Gemini 빈 응답: {}", res);
        return "이 질문에는 답할 수 없어요. 축구 이야기로 다시 물어봐 주세요.";
    }

    private String systemPrompt() {
        return """
                너는 축구 커뮤니티 사이트 FC1878 의 AI 도우미 "FC봇"이야.
                오늘은 %s (한국 시간)이고, 이번 시즌은 %d-%02d 시즌(시즌 값 %d)이야.

                규칙:
                - 경기 일정·결과·순위·선수 기록·이적·해외파 같은 사실은 반드시 도구로 조회한 결과로만 답해. 기억으로 숫자를 지어내지 마.
                - 팀·선수 ID 를 모르면 먼저 search_football 로 찾아. 선수 기록은 search_football → get_player 순서로.
                - "오늘", "내일", "이번 주말" 같은 말은 오늘 날짜로 계산해서 get_fixtures 에 YYYY-MM-DD 로 넣어. 시간은 한국 시간이야.
                - 리그 ID 가 필요하면 list_leagues 로 확인해.
                - 조회해도 데이터가 없으면 "사이트에 아직 그 정보가 없어요"라고 솔직하게 말해. 이 사이트는 주요 유럽 리그·챔피언스리그 위주야.
                - 기록 해석·전망 같은 의견은 데이터를 근거로 짧게 말해도 되지만, 의견이라는 걸 알 수 있게 말해.
                - 축구와 관계없는 질문에는 축구 질문을 해 달라고 정중하게 안내해.
                - 한국어로, 짧고 읽기 쉽게 답해. 목록은 "- " 로 시작하고, 강조는 **굵게** 만 써. 표·제목(#)·링크·ID 숫자는 쓰지 마.
                - 팀·선수 이름은 한글 이름이 있으면 한글로 써.
                """.formatted(tools.today(), tools.season(), (tools.season() + 1) % 100, tools.season());
    }
}
