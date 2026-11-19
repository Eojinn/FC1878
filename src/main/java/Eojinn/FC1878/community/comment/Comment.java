package Eojinn.FC1878.community.comment;

import Eojinn.FC1878.community.post.Post;
import Eojinn.FC1878.global.common.BaseTimeEntity;
import Eojinn.FC1878.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member author;

    @Column(nullable = false, length = 1000)
    private String content;

    public static Comment create(Post post, Member author, String content) {
        Comment c = new Comment();
        c.post = post;
        c.author = author;
        c.content = content;
        return c;
    }

    public boolean isWrittenBy(Long memberId) {
        return author.getId().equals(memberId);
    }
}
