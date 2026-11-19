package Eojinn.FC1878.community.post;

import Eojinn.FC1878.community.post.PostDtos.*;
import Eojinn.FC1878.global.common.PageResponse;
import Eojinn.FC1878.global.error.BusinessException;
import Eojinn.FC1878.global.error.ErrorCode;
import Eojinn.FC1878.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberService memberService;

    @Transactional(readOnly = true)
    public PageResponse<PostSummaryResponse> list(PostCategory category, String keyword, String sort, int page, int size) {
        Sort order = "popular".equals(sort)
                ? Sort.by(Sort.Order.desc("viewCount"), Sort.Order.desc("id"))
                : Sort.by(Sort.Direction.DESC, "id");
        PageRequest pageable = PageRequest.of(page, Math.min(size, 50), order);
        Page<Post> posts;
        if (keyword != null && !keyword.isBlank()) {
            posts = postRepository.findByTitleContainingAndDeletedFalse(keyword.trim(), pageable);
        } else if (category != null) {
            posts = postRepository.findByCategoryAndDeletedFalse(category, pageable);
        } else {
            posts = postRepository.findByDeletedFalse(pageable);
        }
        return PageResponse.from(posts.map(PostSummaryResponse::from));
    }

    @Transactional
    public PostDetailResponse read(Long id) {
        Post post = getPost(id);
        post.increaseView();
        return PostDetailResponse.from(post);
    }

    @Transactional
    public Long create(Long memberId, PostRequest req) {
        Post post = Post.create(memberService.getMember(memberId), req.category(), req.title(), req.content());
        return postRepository.save(post).getId();
    }

    @Transactional
    public PostDetailResponse update(Long memberId, Long id, PostRequest req) {
        Post post = getOwnedPost(memberId, id);
        post.edit(req.category(), req.title(), req.content());
        return PostDetailResponse.from(post);
    }

    @Transactional
    public void delete(Long memberId, Long id) {
        getOwnedPost(memberId, id).delete();
    }

    Post getPost(Long id) {
        return postRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    private Post getOwnedPost(Long memberId, Long id) {
        Post post = getPost(id);
        if (!post.isWrittenBy(memberId)) throw new BusinessException(ErrorCode.FORBIDDEN);
        return post;
    }
}
