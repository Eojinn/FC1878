package Eojinn.FC1878.community.post;

import Eojinn.FC1878.community.post.PostDtos.*;
import Eojinn.FC1878.global.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public PageResponse<PostSummaryResponse> list(@RequestParam(required = false) PostCategory category,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return postService.list(category, page, size);
    }

    @GetMapping("/{id}")
    public PostDetailResponse read(@PathVariable Long id) {
        return postService.read(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> create(@AuthenticationPrincipal Long memberId, @Valid @RequestBody PostRequest req) {
        return Map.of("id", postService.create(memberId, req));
    }

    @PutMapping("/{id}")
    public PostDetailResponse update(@AuthenticationPrincipal Long memberId, @PathVariable Long id,
                                     @Valid @RequestBody PostRequest req) {
        return postService.update(memberId, id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Long memberId, @PathVariable Long id) {
        postService.delete(memberId, id);
    }
}
