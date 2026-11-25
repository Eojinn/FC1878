package Eojinn.FC1878.chat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class ChatDtos {

    private ChatDtos() {
    }

    /** 대화 기록 (마지막이 이번 질문). 화면이 최근 대화만 보냄 */
    public record ChatRequest(@NotEmpty @Size(max = 12) List<@Valid ChatMessage> messages) {
    }

    /** role: user(사용자) 또는 assistant(챗봇) */
    public record ChatMessage(@NotBlank @Pattern(regexp = "user|assistant") String role,
                              @NotBlank @Size(max = 2000) String text) {
    }

    /**
     * @param state OK, NOT_CONFIGURED(키 없음), LIMIT(오늘 질문 수 초과), BUSY(AI 사용량 초과), ERROR
     * @param links 답변에 나온 팀·선수·경기 페이지 바로가기
     * @param remaining 오늘 남은 질문 수
     */
    public record ChatResponse(String state, String answer, List<ChatLink> links, Integer remaining) {
    }

    /** type: team, player, fixture, page */
    public record ChatLink(String type, String label, String url) {
    }

    public record ChatStatus(boolean enabled, int remaining, int dailyLimit, boolean member) {
    }
}
