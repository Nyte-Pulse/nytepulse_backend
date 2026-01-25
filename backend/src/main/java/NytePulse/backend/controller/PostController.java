package NytePulse.backend.controller;

// PostController.java
import NytePulse.backend.dto.PostShareInfoDTO;
import NytePulse.backend.dto.ShareResponseDTO;
import NytePulse.backend.entity.Media;
import NytePulse.backend.entity.Post;
import NytePulse.backend.service.centralServices.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
            @RequestParam(value = "tagFriendIds", required = false) List<String> tagFriendIds,
            @RequestParam(value = "mentionFriendIds", required = false) List<String> mentionFriendIds,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {

        return postService.createPost(content, userId, tagFriendIds, mentionFriendIds, location, files);
    }



    @GetMapping("/getPostsByUser/{userId}")
    public ResponseEntity<?> getPostsByUser(@PathVariable String userId) {
        return postService.getPostsByUser(userId);
    }


    @GetMapping("/getPostById/{id}")
    public ResponseEntity<?> getPostByPostId(@PathVariable Long id) {
        return postService.getPostByPostId(id);
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
            @RequestParam(defaultValue = "20") int size,
            @RequestParam("viewerId") Long viewerId) {
        return postService.getPostForFeed(page, size,viewerId);
    }

    @GetMapping("/getTaggedPosts/{userId}")
    public ResponseEntity<?> getTaggedPosts(@PathVariable String userId) {
        return postService.getTaggedPosts(userId);
    }

    @GetMapping("/getStoriesBySettings/{userId}")
    public ResponseEntity<?> getStoriesBySettings(@PathVariable Long userId) {
        return postService.getStoriesBySettings(userId);
    }

    @PostMapping("/stories/{storyId}/trackView")
    public ResponseEntity<?> viewStory(
            @PathVariable Long storyId,
            @RequestHeader("Authorization") String token) {
        return postService.recordStoryView(storyId, token);
    }

    @GetMapping("/stories/{storyId}/views")
    public ResponseEntity<?> getStoryViewers(@PathVariable Long storyId) {
        return postService.getStoryViewers(storyId);
    }

    @PostMapping("/{postId}/save")
    public ResponseEntity<?> savePost(@PathVariable Long postId, Authentication authentication) {
        String currentUserId = authentication.getName();
        return postService.savePost(currentUserId, postId);
    }

    @DeleteMapping("/{postId}/unsave")
    public ResponseEntity<?> unsavePost(@PathVariable Long postId, Authentication authentication) {
        String currentUserId = authentication.getName();
        return postService.removeSavedPost(currentUserId, postId);
    }


    @GetMapping("/saved")
    public ResponseEntity<?> getSavedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        String currentUserId = authentication.getName();
        return postService.getSavedPosts(currentUserId, page, size);
    }

}
