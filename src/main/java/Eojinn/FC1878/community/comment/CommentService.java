package Eojinn.FC1878.community.comment;

import Eojinn.FC1878.community.post.PostRepository;
import Eojinn.FC1878.global.error.BusinessException;
import Eojinn.FC1878.global.error.ErrorCode;
import Eojinn.FC1878.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberService memberService;

    public record CommentResponse(Long id, Long authorId, String authorNickname, String content,
                                  LocalDateTime createdAt) {
        static CommentResponse from(Comment c) {
            return new CommentResponse(c.getId(), c.getAuthor().getId(), c.getAuthor().getNickname(),
                    c.getContent(), c.getCreatedAt());
        }
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> list(Long postId) {
        return commentRepository.findByPostIdOrderByIdAsc(postId).stream().map(CommentResponse::from).toList();
    }

    @Transactional
    public Long create(Long memberId, Long postId, String content) {
        var post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        return commentRepository.save(Comment.create(post, memberService.getMember(memberId), content)).getId();
    }

    @Transactional
    public void delete(Long memberId, Long commentId) {
        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        if (!c.isWrittenBy(memberId)) throw new BusinessException(ErrorCode.FORBIDDEN);
        commentRepository.delete(c);
    }
}
