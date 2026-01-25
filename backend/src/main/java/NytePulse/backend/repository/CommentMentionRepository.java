package NytePulse.backend.repository;

import NytePulse.backend.entity.CommentMention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentMentionRepository extends JpaRepository<CommentMention, Long> {
}
