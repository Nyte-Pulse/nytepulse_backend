package NytePulse.backend.service.centralServices;

import NytePulse.backend.dto.PostShareInfoDTO;
import NytePulse.backend.entity.Media;
import NytePulse.backend.entity.Post;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


public interface PostService {
    ResponseEntity<?> createPost(String content, String userId,List<String> tagFriendIds , List<String> mentionFriendIds,String location,MultipartFile[] files);

    ResponseEntity<?> getPostsByUser(String userId);

    ResponseEntity<?> generateShareLink(Long postId);

    ResponseEntity<?> getPostShareInfo(Long postId);

    void trackShareClick(Long postId);

    ResponseEntity<?> updatePost(Long postId, String content, String userId,
                    MultipartFile[] newFiles, List<Long> removeMediaIds);
    ResponseEntity<?> updatePostContent(Long postId, String content, String userId);
    ResponseEntity<?> removeMediaFromPost(Long postId, List<Long> mediaIds, String userId);

    ResponseEntity<?> deletePost(Long postId, String userId);

    ResponseEntity<?> getMediasByMediaType(String userId, Media.MediaType mediaType);

    ResponseEntity<?> createStory(String content, String userId, MultipartFile[] files,Boolean isCloseFriendsOnly,Long musicTrackId);

    ResponseEntity<?> getStoriesByUserId(String userId);

    ResponseEntity<?> deleteStory(Long storyId, String userId);

    ResponseEntity<?> viewStoryOnlyForFollowersOrCloseFriends(Long storyId, String viewerId);

    ResponseEntity<?> showStoryOnlyForFollowers(Long storyId, String viewerId);

    ResponseEntity<?> getPostForFeed(int page,int size,Long viewerId);

    ResponseEntity<?> getTaggedPosts(String userId);

    ResponseEntity<?> getStoriesBySettings(Long userId);

    ResponseEntity<?> getPostByPostId(Long id);

    Post getPostById(Long postId);

    ResponseEntity<?> recordStoryView(Long storyId, String token);

    ResponseEntity<?> getStoryViewers(Long storyId);

    ResponseEntity<?> savePost(String userId, Long postId);
    ResponseEntity<?> removeSavedPost(String userId, Long postId);
    ResponseEntity<?> getSavedPosts(String userId, int page, int size);
}
