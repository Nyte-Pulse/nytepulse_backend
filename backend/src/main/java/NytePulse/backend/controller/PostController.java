package NytePulse.backend.controller;

// PostController.java
import NytePulse.backend.dto.PostShareInfoDTO;
import NytePulse.backend.dto.ShareResponseDTO;
import NytePulse.backend.entity.Media;
import NytePulse.backend.entity.Post;
import NytePulse.backend.service.centralServices.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/post")
@CrossOrigin(origins = "*")
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping("/create")
    public ResponseEntity<?> createPost(
            @RequestParam("content") String content,
            @RequestParam("userId") String userId,
            @RequestParam("tagFriendId") String tagFriendId,
            @RequestParam("mentionFriendId") String mentionFriendId,
            @RequestParam("location") String location,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {
       return postService.createPost(content, userId,tagFriendId,mentionFriendId,location, files);
    }


    @GetMapping("/getPostsByUser/{userId}")
    public ResponseEntity<?> getPostsByUser(@PathVariable String userId) {
        return postService.getPostsByUser(userId);
    }


    @GetMapping("/getPostById/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        try {
            Post post = postService.getPostById(id);
            return ResponseEntity.ok(post);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{postId}/share")
    public ResponseEntity<?> generateShareLink(@PathVariable Long postId) {
            return postService.generateShareLink(postId);
    }

    @GetMapping("/share/{postId}")
    public ResponseEntity<?> getSharedPost(@PathVariable Long postId) {
            postService.trackShareClick(postId);
        return postService.getPostShareInfo(postId);
    }

    @GetMapping("/{postId}/share-count")
    public Post getShareCount(@PathVariable Long postId) {
            return postService.getPostById(postId);
    }

    @PutMapping("/updatePost/{postId}")
    public ResponseEntity<?> updatePost(
            @PathVariable Long postId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam("userId") String userId,
            @RequestParam(value = "newFiles", required = false) MultipartFile[] newFiles,
            @RequestParam(value = "removeMediaIds", required = false) String removeMediaIds) {

            List<Long> mediaIdsToRemove = null;
            if (removeMediaIds != null && !removeMediaIds.trim().isEmpty()) {
                mediaIdsToRemove = Arrays.stream(removeMediaIds.split(","))
                        .map(String::trim)
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
            }

            return postService.updatePost(postId, content, userId, newFiles, mediaIdsToRemove);
    }

    @PutMapping("/{postId}/content")
    public ResponseEntity<?> updatePostContent(
            @PathVariable Long postId,
            @RequestParam("content") String content,
            @RequestParam("userId") String userId) {

            return postService.updatePostContent(postId, content, userId);

    }

    @DeleteMapping("/{postId}/media")
    public ResponseEntity<?> removeMediaFromPost(
            @PathVariable Long postId,
            @RequestParam("mediaIds") String mediaIds,
            @RequestParam("userId") String userId) {
            List<Long> mediaIdsToRemove = Arrays.stream(mediaIds.split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

            return postService.removeMediaFromPost(postId, mediaIdsToRemove, userId);
    }

    @DeleteMapping("/deletePost/{postId}")
    public ResponseEntity<?> deletePost(
            @PathVariable Long postId,
            @RequestParam("userId") String userId) {

            return postService.deletePost(postId, userId);
    }

    @GetMapping("/getMediasByMediaType")
    public ResponseEntity<?> getMediasByMediaType(
            @RequestParam("userId") String userId,
            @RequestParam("mediaType") Media.MediaType mediaType) {
        return postService.getMediasByMediaType(userId, mediaType);
    }

    @PostMapping("/createStory")
    public ResponseEntity<?> createStory(
            @RequestParam("content") String content,
            @RequestParam("userId") String userId,
            @RequestParam(value = "files", required = false) MultipartFile[] files,Boolean isCloseFriendsOnly) {
        return postService.createStory(content, userId, files,isCloseFriendsOnly);
    }

    @GetMapping("/getStories/{userId}")
    public ResponseEntity<?> getStoriesByUserId(@PathVariable String userId) {
        return postService.getStoriesByUserId(userId);
    }

    @DeleteMapping("/deleteStory/{storyId}")
    public ResponseEntity<?> deleteStory(
            @PathVariable Long storyId,
            @RequestParam("userId") String userId) {
        return postService.deleteStory(storyId, userId);
    }

    @GetMapping("/viewStoryOnlyForFollowersOrCloseFriends/{storyId}")
    public ResponseEntity<?> viewStoryOnlyForFollowersOrCloseFriends(
            @PathVariable Long storyId,
            @RequestParam("viewerId") String viewerId) {
        return postService.viewStoryOnlyForFollowersOrCloseFriends(storyId, viewerId);
    }

    @GetMapping("/showStoryOnlyForFollowers/{storyId}")
    public ResponseEntity<?> showStoryOnlyForFollowers(
            @PathVariable Long storyId,
            @RequestParam("viewerId") String viewerId) {
        return postService.showStoryOnlyForFollowers(storyId, viewerId);
    }

    @GetMapping("/feed")
    public ResponseEntity<?> getPostForFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return postService.getPostForFeed(page, size);
    }

    @GetMapping("/getTaggedPosts/{userId}")
    public ResponseEntity<?> getTaggedPosts(@PathVariable String userId) {
        return postService.getTaggedPosts(userId);
    }

    @GetMapping("/getStoriesBySettings/{userId}")
    public ResponseEntity<?> getStoriesBySettings(@PathVariable String userId) {
        return postService.getStoriesBySettings(userId);
    }

}
