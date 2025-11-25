package NytePulse.backend.repository;

import NytePulse.backend.entity.Media;
import NytePulse.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    Media  findByPost_Id(Long id);

    List<Media> findByPostId(Long postId);

    @Query("SELECT m FROM Media m WHERE m.id IN :mediaIds AND m.post.id = :postId")
    List<Media> findByIdsAndPostId(@Param("mediaIds") List<Long> mediaIds, @Param("postId") Long postId);

    @Query("SELECT m FROM Media m WHERE m.id IN :mediaIds AND m.post.user.id = :userId")
    List<Media> findByIdsAndUserId(@Param("mediaIds") List<Long> mediaIds, @Param("userId") Long userId);

    List<Media> findByPostUserAndMediaType(User user, Media.MediaType mediaType);

}