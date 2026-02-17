package NytePulse.backend.repository;

import NytePulse.backend.entity.PostReaction;
import NytePulse.backend.enums.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {

    Optional<PostReaction> findByPostIdAndUserId(Long postId, Long userId);

    Long countByPostId(Long postId);

    Long countByPostIdAndReactionType(Long postId, ReactionType reactionType);

    @Query("SELECT r.reactionType, COUNT(r) FROM PostReaction r WHERE r.post.id = :postId GROUP BY r.reactionType")
    List<Object[]> countReactionsByPostIdGrouped(Long postId);
}