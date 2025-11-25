package NytePulse.backend.service.centralServices;

import NytePulse.backend.dto.PostShareInfoDTO;
import NytePulse.backend.entity.Media;
import NytePulse.backend.entity.Post;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


public interface PostService {
    ResponseEntity<?> createPost(String content, String userId, MultipartFile[] files);

    ResponseEntity<?> getPostsByUser(String userId);

    Post getPostById(Long id);

    ResponseEntity<?> generateShareLink(Long postId);

    ResponseEntity<?> getPostShareInfo(Long postId);

    void trackShareClick(Long postId);

    ResponseEntity<?> updatePost(Long postId, String content, String userId,
                    MultipartFile[] newFiles, List<Long> removeMediaIds);
    ResponseEntity<?> updatePostContent(Long postId, String content, String userId);
    ResponseEntity<?> removeMediaFromPost(Long postId, List<Long> mediaIds, String userId);

    ResponseEntity<?> deletePost(Long postId, String userId);

    ResponseEntity<?> getMediasByMediaType(String userId, Media.MediaType mediaType);
}
