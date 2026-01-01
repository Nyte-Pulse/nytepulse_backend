package NytePulse.backend.repository;

import NytePulse.backend.entity.PostMention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostMentionRepository extends JpaRepository<PostMention, Long> {
    List<PostMention> findByPostId(Long postId);
    List<PostMention> findByMentionedUserId(String mentionedUserId);
    void deleteByPostId(Long postId);
}
