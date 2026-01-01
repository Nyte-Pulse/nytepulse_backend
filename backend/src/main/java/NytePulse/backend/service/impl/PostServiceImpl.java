package NytePulse.backend.service.impl;

import NytePulse.backend.dto.*;

import NytePulse.backend.entity.*;
import NytePulse.backend.enums.PostVisibility;
import NytePulse.backend.enums.StoryVisibility;
import NytePulse.backend.exception.ResourceNotFoundException;
import NytePulse.backend.repository.*;
import NytePulse.backend.service.BunnyNetService;
import NytePulse.backend.service.centralServices.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private static final Logger log = LoggerFactory.getLogger(PostServiceImpl.class);


    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MediaRepository mediaRepository;
    @Autowired
    private BunnyNetService bunnyNetService;

    @Autowired
    private ClubDetailsRepository clubDetailsRepository;

    @Autowired
    private UserDetailsRepository  userDetailsRepository;

    @Autowired
    private UserRelationshipRepository userRelationshipRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private CloseFriendServiceImpl closeFriendServiceImpl;

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public ResponseEntity<?> createPost(String content, String userId,String tagFriendId,String mentionFriendId,String location, MultipartFile[] files) {
        try {
            if (content == null || content.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Content is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            User user = userRepository.findByUserId(userId);

            // Create and save the post
            Post post = new Post();
            post.setContent(content);
            post.setUser(user);
            post.setLocation(location);
            post.setTagFriendId(tagFriendId);
            post.setMentionFriendId(mentionFriendId);
            Post savedPost = postRepository.save(post);

            // Process and upload files to BunnyNet
            List<Media> mediaList = new ArrayList<>();

            if (files != null) {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        Media media = processAndUploadFile(file, savedPost);
                        mediaList.add(mediaRepository.save(media));
                    }
                }
            }

            savedPost.setMedia(mediaList);
            return ResponseEntity.ok(savedPost);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create Post");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private Media processAndUploadFile(MultipartFile file, Post post) throws IOException {
        String contentType = file.getContentType();
        BunnyNetUploadResult result;

        if (contentType != null && contentType.startsWith("image/")) {
            result = bunnyNetService.uploadImage(file);
        } else if (contentType != null && contentType.startsWith("video/")) {
            String title = "Video for post " + post.getId();
            result = bunnyNetService.uploadVideo(file, title);
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + contentType);
        }

        Media media = new Media();
        media.setFileName(result.getFileName());
        media.setFileType(contentType);
        media.setBunnyUrl(result.getCdnUrl());
        media.setBunnyVideoId(result.getBunnyVideoId());
        media.setFileSize(result.getFileSize());
        media.setMediaType(result.getMediaType());
        media.setCreatedAt(ZonedDateTime.now());
        media.setPost(post);

        return media;
    }

    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public ResponseEntity<?> getPostsByUser(String userId) {
        try {
            User user = userRepository.findByUserId(userId);
            if (user == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            List<Post> posts = postRepository.findByUserOrderByCreatedAtDesc(user);
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("posts", posts);
            response.put("postCount", posts.size());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve posts");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }


    @Override
    public ResponseEntity<?> generateShareLink(Long postId) {
        try {
            Post post = getPostById(postId);
            // Generate share URL
            String shareUrl = baseUrl + "/api/posts/share/" + postId;

            // Create share text
            String shareText = createShareText(post);

            Map<String, Object> response = new HashMap<>();
            response.put("shareUrl", shareUrl);
            response.put("shareText", shareText);
            response.put("postId", postId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to generate share link");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    public ResponseEntity<?> getPostShareInfo(Long postId) {
        try {
            Post post = getPostById(postId);

            PostShareInfoDTO shareInfo = new PostShareInfoDTO();
            shareInfo.setId(post.getId());
            shareInfo.setContent(post.getContent());
            shareInfo.setAuthorUsername(post.getUser().getUsername());
            shareInfo.setAuthorUserId(post.getUser().getUserId());
            shareInfo.setCreatedAt(post.getCreatedAt());
            shareInfo.setShareCount(post.getShareCount());
            shareInfo.setShareUrl(baseUrl + "/api/posts/share/" + postId);

            // Get first media URL if available
            if (post.getMedia() != null && !post.getMedia().isEmpty()) {
                Media firstMedia = post.getMedia().get(0);
                shareInfo.setFirstMediaUrl(firstMedia.getBunnyUrl());
                shareInfo.setMediaType(firstMedia.getMediaType().toString());
            }

            return ResponseEntity.ok(shareInfo);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get Post Share Info");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    public void trackShareClick(Long postId) {
        postRepository.incrementShareCount(postId);
    }


    private String createShareText(Post post) {
        String content = post.getContent();
        if (content.length() > 100) {
            content = content.substring(0, 100) + "...";
        }

        return String.format(
                "Check out this post by %s: %s - Shared via NytePulse",
                post.getUser().getUsername(),
                content
        );
    }

    @Override
    public ResponseEntity<?> updatePost(Long postId, String content, String userId,
                                        MultipartFile[] newFiles, List<Long> removeMediaIds) {

        try {
            Post post = getPostById(postId);

            // Update content if provided
            if (content != null && !content.trim().isEmpty()) {
                post.setContent(content);
            }

            // Remove specified media
            if (removeMediaIds != null && !removeMediaIds.isEmpty()) {
                removeMediaFromPost(postId, removeMediaIds, userId);
            }

            // Add new media files
            if (newFiles != null && newFiles.length > 0) {
                List<Media> newMediaList = new ArrayList<>();
                for (MultipartFile file : newFiles) {
                    if (!file.isEmpty()) {
                        Media media = processAndUploadFile(file, post);
                        newMediaList.add(mediaRepository.save(media));
                    }
                }

                // Add new media to existing media list
                if (post.getMedia() == null) {
                    post.setMedia(new ArrayList<>());
                }
                post.getMedia().addAll(newMediaList);
            }

            // Set updated timestamp
            post.setUpdatedAt(LocalDateTime.now());

            Post updatedPost = postRepository.save(post);
            return ResponseEntity.ok(updatedPost);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update Post");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

    }

    @Override
    public ResponseEntity<?> updatePostContent(Long postId, String content, String userId) {

        try {
            Post post = getPostById(postId);
            post.setContent(content);
            post.setUpdatedAt(LocalDateTime.now());

            Post updatedPost = postRepository.save(post);
            return ResponseEntity.ok(updatedPost);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update Post Content");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    public ResponseEntity<?> removeMediaFromPost(Long postId, List<Long> mediaIds, String userId) {

        try {
            List<Media> mediaToRemove = mediaRepository.findByIdsAndPostId(mediaIds, postId);

            if (mediaToRemove.size() != mediaIds.size()) {
                throw new RuntimeException("Some media files not found or don't belong to this post");
            }

            List<String> successfulDeletions = new ArrayList<>();
            List<String> failedDeletions = new ArrayList<>();

            // Delete from BunnyNet first, then from database
            for (Media media : mediaToRemove) {
                boolean deletedFromBunny = bunnyNetService.deleteMedia(
                        media.getFileName(),
                        media.getBunnyVideoId(),
                        media.getMediaType()
                );

                if (deletedFromBunny) {
                    successfulDeletions.add(media.getFileName());
                    log.info("Successfully deleted media from BunnyNet: {}", media.getFileName());
                } else {
                    failedDeletions.add(media.getFileName());
                    log.warn("Failed to delete media from BunnyNet: {}", media.getFileName());
                }
            }

            mediaRepository.deleteAll(mediaToRemove);

            return ResponseEntity.ok(Map.of(
                    "message", "Media removal process completed",
                    "successfulDeletions", successfulDeletions,
                    "failedDeletions", failedDeletions
            ));
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to remove Media From Post");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

    }


    @Override
    public ResponseEntity<?> deletePost(Long postId, String userId) {
        try {
            Post post = getPostById(postId);
            if (!post.getUser().getUserId().equals(userId)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Unauthorized");
                errorResponse.put("message", "You can only delete your own posts");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            // Delete associated media from BunnyNet
            List<Media> mediaList = post.getMedia();
            if (mediaList != null && !mediaList.isEmpty()) {
                for (Media media : mediaList) {
                    boolean deletedFromBunny = bunnyNetService.deleteMedia(
                            media.getFileName(),
                            media.getBunnyVideoId(),
                            media.getMediaType()
                    );

                    if (deletedFromBunny) {
                        log.info("Successfully deleted media from BunnyNet: {}", media.getFileName());
                    } else {
                        log.warn("Failed to delete media from BunnyNet: {}", media.getFileName());
                    }
                }
            }

            postRepository.delete(post);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Post deleted successfully");
            response.put("postId", postId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete Post");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    public ResponseEntity<?> getMediasByMediaType(String userId, Media.MediaType mediaType) {
        try {
            User user = userRepository.findByUserId(userId);
            if (user == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            List<Media> medias = mediaRepository.findByPostUserAndMediaType(user, mediaType);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("mediaType", mediaType);
            response.put("medias", medias);
            response.put("mediaCount", medias.size());
            response.put("status", HttpStatus.OK.value());


            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve medias by media type");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    public ResponseEntity<?> createStory(String content, String userId, MultipartFile[] files,Boolean isCloseFriendsOnly) {
        try {
            if (content == null || content.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Content is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            User user = userRepository.findByUserId(userId);

            Story story = new Story();          // Use Story entity instead of Post
            story.setContent(content);
            story.setUser(user);
            story.setIsCloseFriendsOnly(isCloseFriendsOnly != null ? isCloseFriendsOnly : false);
            Story savedStory = storyRepository.save(story);

            List<Media> mediaList = new ArrayList<>();

            if (files != null) {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        Media media = processAndUploadStoryFile(file, savedStory);
                        mediaList.add(mediaRepository.save(media));
                    }
                }
            }

            savedStory.setMedia(mediaList);
            return ResponseEntity.ok(savedStory);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create Story");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    private Media processAndUploadStoryFile(MultipartFile file, Story story) throws IOException {
        String contentType = file.getContentType();
        BunnyNetUploadResult result;

        if (contentType != null && contentType.startsWith("image/")) {
            // Pass folder name "stories" for image upload
            result = bunnyNetService.uploadImageToFolder(file, "stories");
        } else if (contentType != null && contentType.startsWith("video/")) {
            String title = "Story Video for story " + story.getId();
            // Pass folder name "stories" for video upload
            result = bunnyNetService.uploadVideoToFolder(file, title, "stories");
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + contentType);
        }

        Media media = new Media();
        media.setFileName(result.getFileName());
        media.setFileType(contentType);
        media.setBunnyUrl(result.getCdnUrl());
        media.setBunnyVideoId(result.getBunnyVideoId());
        media.setFileSize(result.getFileSize());
        media.setMediaType(result.getMediaType());
        media.setStory(story);  // Assuming Media has story relation

        return media;
    }

    public ResponseEntity<?> getStoriesForUser(String viewerUserId, String targetUserId) {
        try {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Colombo"));
            List<Story> allStories = storyRepository
                    .findByUserUserIdAndExpiresAtAfterOrderByCreatedAtDesc(targetUserId, now);

            // Filter stories based on close friends setting
            List<Story> visibleStories = allStories.stream()
                    .filter(story -> {
                        if (!story.getIsCloseFriendsOnly()) {
                            return true; // Public story
                        }
                        // Close friends only story - check if viewer is in close friends
                        return closeFriendServiceImpl.isCloseFriend(targetUserId, viewerUserId);
                    })
                    .collect(Collectors.toList());

            // Convert to DTO...
            return ResponseEntity.ok(visibleStories);

        } catch (Exception e) {
            log.error("Error fetching stories: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch stories");
        }
    }

    @Override
    public ResponseEntity<?> getStoriesByUserId(String userId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Story> stories = storyRepository
                    .findByUserUserIdAndExpiresAtAfterOrderByCreatedAtDesc(userId, now);

            UserDetails userDetails=userDetailsRepository.findByUserId(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("profilePicture", userDetails.getProfilePicture());
            response.put("totalStories", stories.size());
            response.put("stories", stories);
            response.put("status", HttpStatus.OK.value());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching stories for user: {}", userId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch stories");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> deleteStory(Long storyId, String userId) {
        try {
            Story story = storyRepository.findById(storyId)
                    .orElseThrow(() -> new RuntimeException("Story not found"));

            // Verify the user owns this story
            if (!story.getUser().getUserId().equals(userId)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Unauthorized");
                errorResponse.put("message", "You can only delete your own stories");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }

            // Delete media files from BunnyNet
            if (story.getMedia() != null) {
                for (Media media : story.getMedia()) {
                    deleteMediaFromBunnyNet(media);
                }
            }

            // Delete story from database
            storyRepository.delete(story);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Story deleted successfully");
            response.put("storyId", storyId);
            response.put("status", HttpStatus.OK.value());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting story ID {}: {}", storyId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete story");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    public ResponseEntity<?> viewStoryOnlyForFollowersOrCloseFriends(Long storyId, String viewerUserId) {
        try {
            Story story = storyRepository.findById(storyId)
                    .orElseThrow(() -> new RuntimeException("Story not found"));

            // Check if the story is close friends only
            if (story.getIsCloseFriendsOnly()) {
                // Verify if viewer is a close friend
                boolean isCloseFriend = closeFriendServiceImpl.isCloseFriend(
                        story.getUser().getUserId(), viewerUserId);
                if (!isCloseFriend) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Access Denied");
                    errorResponse.put("message", "This story is only for close friends");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
                }
            }

            return ResponseEntity.ok(story);

        } catch (Exception e) {
            log.error("Error viewing story ID {}: {}", storyId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to view story");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    public ResponseEntity<?> showStoryOnlyForFollowers(Long storyId, String viewerUserId){
        try {
            Story story = storyRepository.findById(storyId)
                    .orElseThrow(() -> new RuntimeException("Story not found"));

            boolean isFollower = checkIfFollower(story.getUser().getUserId(), viewerUserId);

            if (!isFollower) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Access Denied");
                errorResponse.put("message", "This story is only for followers");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }

            return ResponseEntity.ok(story);

        } catch (Exception e) {
            log.error("Error viewing story ID {}: {}", storyId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to view story");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private boolean checkIfFollower(String userId, String viewerUserId) {

        return userRelationshipRepository
                .existsByFollower_UserIdAndFollowing_UserId(viewerUserId, userId);

    }

    private void deleteMediaFromBunnyNet(Media media) {
        try {
            if (media.getMediaType() == Media.MediaType.IMAGE) {
                bunnyNetService.deleteImageFromFolder(media.getFileName(), "stories");
            } else if (media.getMediaType() == Media.MediaType.VIDEO) {
                bunnyNetService.deleteVideo(media.getBunnyVideoId());
            }
            log.info("Deleted media file: {}", media.getFileName());
        } catch (Exception e) {
            log.error("Failed to delete media file {}: {}", media.getFileName(), e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getPostForFeed(
            int page,
             int size,
            Long viewerId) {
        try {
            User viewer = userRepository.findById(viewerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Viewer not found"));

            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Post> postPage = postRepository.findVisiblePostsForUser(viewerId, pageable);

            Set<String> allUserIds = new HashSet<>();
            Set<String> personalUserIds = new HashSet<>();
            Set<String> businessUserIds = new HashSet<>();

            // Filter posts based on visibility settings
            List<Post> visiblePosts = postPage.getContent().stream()
                    .filter(post -> canViewPost(post, viewerId))
                    .collect(Collectors.toList());

            visiblePosts.forEach(post -> {
                String postUserId = post.getUser().getUserId();
                allUserIds.add(postUserId);

                if (postUserId.startsWith("BS")) {
                    businessUserIds.add(postUserId);
                } else {
                    personalUserIds.add(postUserId);
                }

                if (post.getTagFriendId() != null) {
                    allUserIds.add(post.getTagFriendId());
                    if (post.getTagFriendId().startsWith("BS")) {
                        businessUserIds.add(post.getTagFriendId());
                    } else {
                        personalUserIds.add(post.getTagFriendId());
                    }
                }

                if (post.getMentionFriendId() != null) {
                    allUserIds.add(post.getMentionFriendId());
                    if (post.getMentionFriendId().startsWith("BS")) {
                        businessUserIds.add(post.getMentionFriendId());
                    } else {
                        personalUserIds.add(post.getMentionFriendId());
                    }
                }
            });

            List<User> usersList = userRepository.findByUserIdIn(new ArrayList<>(allUserIds));

            List<UserDetails> userDetailsList = personalUserIds.isEmpty() ?
                    Collections.emptyList() :
                    userDetailsRepository.findByUserIdIn(new ArrayList<>(personalUserIds));

            List<ClubDetails> clubDetailsList = businessUserIds.isEmpty() ?
                    Collections.emptyList() :
                    clubDetailsRepository.findByUserIdIn(new ArrayList<>(businessUserIds));

            Map<String, User> usersMap = usersList.stream()
                    .collect(Collectors.toMap(User::getUserId, u -> u));

            Map<String, UserDetails> userDetailsMap = userDetailsList.stream()
                    .collect(Collectors.toMap(UserDetails::getUserId, ud -> ud));

            Map<String, ClubDetails> clubDetailsMap = clubDetailsList.stream()
                    .collect(Collectors.toMap(ClubDetails::getUserId, cd -> cd));

            List<Map<String, Object>> enrichedPosts = visiblePosts.stream()
                    .map(post -> {
                        Map<String, Object> postData = new HashMap<>();

                        postData.put("id", post.getId());
                        postData.put("content", post.getContent());
                        postData.put("location", post.getLocation());
                        postData.put("createdAt", post.getCreatedAt());
                        postData.put("updatedAt", post.getUpdatedAt());
                        postData.put("shareCount", post.getShareCount());
                        postData.put("media", post.getMedia());
                        postData.put("likesCount", post.getLikes().size());
                        postData.put("commentsCount", post.getComments().size());

                        User user = post.getUser();
                        Map<String, Object> userInfo = buildUserInfo(
                                user,
                                userDetailsMap.get(user.getUserId()),
                                clubDetailsMap.get(user.getUserId())
                        );
                        postData.put("userDetails", userInfo);

                        if (post.getTagFriendId() != null) {
                            User taggedUser = usersMap.get(post.getTagFriendId());
                            if (taggedUser != null) {
                                Map<String, Object> taggedFriendInfo = buildUserInfo(
                                        taggedUser,
                                        userDetailsMap.get(post.getTagFriendId()),
                                        clubDetailsMap.get(post.getTagFriendId())
                                );
                                postData.put("taggedFriend", taggedFriendInfo);
                            }
                        }

                        if (post.getMentionFriendId() != null) {
                            User mentionedUser = usersMap.get(post.getMentionFriendId());
                            if (mentionedUser != null) {
                                Map<String, Object> mentionedFriendInfo = buildUserInfo(
                                        mentionedUser,
                                        userDetailsMap.get(post.getMentionFriendId()),
                                        clubDetailsMap.get(post.getMentionFriendId())
                                );
                                postData.put("mentionedFriend", mentionedFriendInfo);
                            }
                        }

                        return postData;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("posts", enrichedPosts);
            response.put("currentPage", postPage.getNumber());
            response.put("totalPages", postPage.getTotalPages());
            response.put("totalPosts", (long) visiblePosts.size());
            response.put("hasNext", postPage.hasNext());
            response.put("hasPrevious", postPage.hasPrevious());
            response.put("status", HttpStatus.OK.value());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching feed posts: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Failed to fetch feed posts",
                    "message", e.getMessage()
            ));
        }
    }

    private boolean canViewPost(Post post, Long viewerId) {
        Long postOwnerId = post.getUser().getId();

        // User can always see their own posts
        if (postOwnerId.equals(viewerId)) {
            return true;
        }

        // Get post owner's settings
        UserSettings postOwnerSettings = userSettingsRepository
                .findByUserId(post.getUser().getId())
                .orElse(null);

        // Default to FOLLOWERS if no settings exist
        if (postOwnerSettings == null) {
            return userRelationshipRepository.existsByFollower_IdAndFollowing_Id(
                    viewerId, postOwnerId);
        }

        PostVisibility visibility = postOwnerSettings.getPostVisibility();

        switch (visibility) {
            case EVERYONE:
                return true;
            case FOLLOWERS:
                return userRelationshipRepository.existsByFollower_IdAndFollowing_Id(
                        viewerId, postOwnerId);
            case PRIVATE:
                return false;
            default:
                return false;
        }
    }


    // Helper method to build user info from either UserDetails or ClubDetails
    private Map<String, Object> buildUserInfo(User user, UserDetails userDetails, ClubDetails clubDetails) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", user.getUserId());
        userInfo.put("username", user.getUsername());

        // Check if it's a business account (userId starts with BS)
        if (user.getUserId().startsWith("BS") && clubDetails != null) {
            userInfo.put("name", clubDetails.getName());
            userInfo.put("profilePicture", clubDetails.getProfilePicture());
            userInfo.put("profilePictureFileName", clubDetails.getProfilePictureFileName());
            userInfo.put("accountType", "BUSINESS");
        }
        // Personal account
        else if (userDetails != null) {
            userInfo.put("name", userDetails.getName());
            userInfo.put("profilePicture", userDetails.getProfilePicture());
            userInfo.put("profilePictureFileName", userDetails.getProfilePictureFileName());
            userInfo.put("accountType", "PERSONAL");
        }

        return userInfo;
    }

    @Override
    public ResponseEntity<?> getTaggedPosts(String userId){
        try {
            User user = userRepository.findByUserId(userId);
            if (user == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            List<Post> taggedPosts = postRepository.findByTagFriendIdOrderByCreatedAtDesc(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("taggedPosts", taggedPosts);
            response.put("taggedPostCount", taggedPosts.size());

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve tagged posts");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    public ResponseEntity<?> getStoriesBySettings(Long viewerId) {
        try {
            // Fetch viewer
            User viewer = userRepository.findById(viewerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Viewer not found"));

            LocalDateTime now = LocalDateTime.now();

            // Fetch all non-expired stories from all users
            List<Story> allStories = storyRepository.findByExpiresAtAfterOrderByCreatedAtDesc(now);

            // Filter stories based on each owner's settings and viewer's permissions
            List<Story> visibleStories = allStories.stream()
                    .filter(story -> canViewStory(story, viewerId))
                    .collect(Collectors.toList());

            // Group stories by user
            Map<String, List<Story>> storiesByUser = visibleStories.stream()
                    .collect(Collectors.groupingBy(
                            story -> story.getUser().getUserId(),
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));

            // Convert to DTO format
            List<UserStoriesDTO> userStoriesList = storiesByUser.entrySet().stream()
                    .map(entry -> {
                        List<Story> userStories = entry.getValue();
                        User storyOwner = userStories.get(0).getUser();

                        // Map user info
                        StoryUserDTO userDTO = StoryUserDTO.builder()
                                .id(storyOwner.getId())
                                .userId(storyOwner.getUserId())
                                .username(storyOwner.getUsername())
                                .accountType(storyOwner.getAccountType().toString())
                                .build();

                        // Map stories
                        List<StoryResponseDTO> storyDTOs = userStories.stream()
                                .map(this::mapToStoryDTO)
                                .collect(Collectors.toList());

                        return UserStoriesDTO.builder()
                                .user(userDTO)
                                .storyCount(storyDTOs.size())
                                .stories(storyDTOs)
                                .build();
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("totalUsers", userStoriesList.size());
            response.put("totalStories", visibleStories.size());
            response.put("userStories", userStoriesList);
            response.put("status", HttpStatus.OK.value());

            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Resource not found");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            log.error("Error fetching stories feed for viewer: {}", viewerId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch stories");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private StoryResponseDTO mapToStoryDTO(Story story) {
        List<StoryMediaDTO> mediaDTOs = story.getMedia().stream()
                .map(media -> StoryMediaDTO.builder()
                        .id(media.getId())
                        .bunnyUrl(media.getBunnyUrl())
                        .mediaType(media.getMediaType().toString())
                        .fileType(media.getFileType())
                        .build())
                .collect(Collectors.toList());

        return StoryResponseDTO.builder()
                .id(story.getId())
                .content(story.getContent())
                .media(mediaDTOs)
                .createdAt(story.getCreatedAt())
                .expiresAt(story.getExpiresAt())
                .isCloseFriendsOnly(story.getIsCloseFriendsOnly())
                .build();
    }


    private boolean canViewStory(Story story, Long viewerId) {
        User storyOwner = story.getUser();
        Long storyOwnerId = storyOwner.getId();

        // User can always see their own stories
        if (storyOwnerId.equals(viewerId)) {
            return true;
        }

        // Check if story is close friends only first
        if (story.getIsCloseFriendsOnly()) {
            // Only close friends can view this story
            User viewer = userRepository.findById(viewerId).orElse(null);
            if (viewer == null) {
                return false;
            }
            return closeFriendServiceImpl.isCloseFriend(
                    storyOwner.getUserId(), viewer.getUserId());
        }

        // Get story owner's settings
        UserSettings ownerSettings = userSettingsRepository
                .findByUserId(storyOwner.getId())
                .orElse(null);

        // Check story visibility based on owner's settings
        return canViewStoriesBasedOnSettings(storyOwnerId, viewerId, ownerSettings);
    }

    private boolean canViewStoriesBasedOnSettings(Long storyOwnerId, Long viewerId, UserSettings ownerSettings) {
        // Default to FOLLOWERS if no settings exist
        if (ownerSettings == null) {
            return userRelationshipRepository.existsByFollower_IdAndFollowing_Id(
                    viewerId, storyOwnerId);
        }

        StoryVisibility visibility = ownerSettings.getStoryVisibility();

        switch (visibility) {
            case EVERYONE:
                return true;

            case FOLLOWERS:
                return userRelationshipRepository.existsByFollower_IdAndFollowing_Id(
                        viewerId, storyOwnerId);

            case CLOSE_FRIENDS:
                // Only close friends can see ALL stories when this setting is enabled
                User storyOwner = userRepository.findById(storyOwnerId).orElse(null);
                User viewer = userRepository.findById(viewerId).orElse(null);
                if (storyOwner != null && viewer != null) {
                    return closeFriendServiceImpl.isCloseFriend(
                            storyOwner.getUserId(), viewer.getUserId());
                }
                return false;

            default:
                return false;
        }
    }




}