package Eojinn.FC1878.community.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = "author")
    Page<Post> findByDeletedFalse(Pageable pageable);

    @EntityGraph(attributePaths = "author")
    Page<Post> findByCategoryAndDeletedFalse(PostCategory category, Pageable pageable);

    @EntityGraph(attributePaths = "author")
    Page<Post> findByTitleContainingAndDeletedFalse(String keyword, Pageable pageable);

    @EntityGraph(attributePaths = "author")
    Optional<Post> findByIdAndDeletedFalse(Long id);
}
