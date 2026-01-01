package NytePulse.backend.repository;

import NytePulse.backend.entity.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostTagRepository extends JpaRepository<PostTag, Long> {
    List<PostTag> findByPostId(Long postId);
    List<PostTag> findByTaggedUserId(String taggedUserId);
    void deleteByPostId(Long postId);
}
