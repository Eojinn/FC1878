package Eojinn.FC1878.community.comment;

import Eojinn.FC1878.community.comment.CommentService.CommentResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    public record CommentRequest(@NotBlank @Size(max = 1000) String content) {
    }

    @GetMapping("/api/posts/{postId}/comments")
    public List<CommentResponse> list(@PathVariable Long postId) {
        return commentService.list(postId);
    }

    @PostMapping("/api/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> create(@AuthenticationPrincipal Long memberId, @PathVariable Long postId,
                                    @Valid @RequestBody CommentRequest req) {
        return Map.of("id", commentService.create(memberId, postId, req.content()));
    }

    @DeleteMapping("/api/comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Long memberId, @PathVariable Long id) {
        commentService.delete(memberId, id);
    }
}
