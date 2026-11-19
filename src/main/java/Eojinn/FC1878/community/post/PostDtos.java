package Eojinn.FC1878.community.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class PostDtos {
    private PostDtos() {
    }

    public record PostRequest(@NotNull PostCategory category,
                              @NotBlank @Size(max = 200) String title,
                              @NotBlank @Size(max = 20000) String content) {
    }

    public record PostSummaryResponse(Long id, String category, String title, String authorNickname,
                                      int viewCount, LocalDateTime createdAt) {
        public static PostSummaryResponse from(Post p) {
            return new PostSummaryResponse(p.getId(), p.getCategory().name(), p.getTitle(),
                    p.getAuthor().getNickname(), p.getViewCount(), p.getCreatedAt());
        }
    }

    public record PostDetailResponse(Long id, String category, String title, String content,
                                     Long authorId, String authorNickname, int viewCount,
                                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        public static PostDetailResponse from(Post p) {
            return new PostDetailResponse(p.getId(), p.getCategory().name(), p.getTitle(), p.getContent(),
                    p.getAuthor().getId(), p.getAuthor().getNickname(), p.getViewCount(),
                    p.getCreatedAt(), p.getUpdatedAt());
        }
    }
}
