package Eojinn.FC1878.chat;

import Eojinn.FC1878.chat.ChatDtos.ChatRequest;
import Eojinn.FC1878.chat.ChatDtos.ChatResponse;
import Eojinn.FC1878.chat.ChatDtos.ChatStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** AI 축구 챗봇. 로그인 없이도 쓸 수 있고, 로그인하면 하루 질문 수가 늘어남 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /** 챗봇 사용 가능 여부 + 오늘 남은 질문 수 */
    @GetMapping("/status")
    public ChatStatus status(@AuthenticationPrincipal Long memberId, HttpServletRequest request) {
        return chatService.status(memberId, request.getRemoteAddr());
    }

    /** 질문: POST /api/chat {"messages":[{"role":"user","text":"손흥민 이번 시즌 기록"}]} */
    @PostMapping
    public ChatResponse chat(@AuthenticationPrincipal Long memberId, HttpServletRequest request,
                             @Valid @RequestBody ChatRequest req) {
        return chatService.chat(req.messages(), memberId, request.getRemoteAddr());
    }
}
