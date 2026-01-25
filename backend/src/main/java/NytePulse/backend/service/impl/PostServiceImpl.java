package NytePulse.backend.service.impl;

import NytePulse.backend.dto.*;

import NytePulse.backend.entity.*;
import NytePulse.backend.enums.NotificationType;
import NytePulse.backend.enums.PostVisibility;
import NytePulse.backend.enums.StoryVisibility;
import NytePulse.backend.exception.ResourceNotFoundException;
import NytePulse.backend.repository.*;
import NytePulse.backend.service.BunnyNetService;
import NytePulse.backend.service.NotificationService;
import NytePulse.backend.service.centralServices.PostService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.scheduling.annotation.Scheduled;
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
    private SavedPostRepository savedPostRepository;

    @Autowired
    private StoryViewRepository storyViewRepository;
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
    private PostTagRepository postTagRepository;

    @Autowired
    private PostMentionRepository postMentionRepository;

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    @Autowired
    private NotificationService notificationService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    @Transactional
    public ResponseEntity<?> createPost(
            String content,
            String userId,
            List<String> tagFriendIds,
            List<String> mentionFriendIds,
            String location,
            MultipartFile[] files) {
        try {
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Content is required"));
            }

            User user = userRepository.findByUserId(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }

            Post post = new Post();
            post.setContent(content);
            post.setUser(user);
            post.setLocation(location);
            Post savedPost = postRepository.save(post);

            List<TaggedUserDTO> taggedUserDTOs = new ArrayList<>();
            if (tagFriendIds != null && !tagFriendIds.isEmpty()) {
                for (String taggedUserId : tagFriendIds) {
                    User taggedUser = userRepository.findByUserId(taggedUserId);
                    if (taggedUser != null) {
                        PostTag postTag = PostTag.builder()
                                .post(savedPost)
                                .taggedUser(taggedUser)
                                .taggedUserId(taggedUserId)
                                .build();
                        postTagRepository.save(postTag);

                        taggedUserDTOs.add(TaggedUserDTO.builder()
                                .userId(taggedUser.getUserId())
                                .username(taggedUser.getUsername())
                                .build());

                        User taggedUserDetails = userRepository.findByUserId(taggedUserId);
                        User creator = userRepository.findByUserId(userId);

                        // ✅ NOTIFICATION: Send Tag Notification
                        String notifMsg = user.getUsername() + " tagged you in a post.";
                        notificationService.createNotification(
                                taggedUserDetails.getId(),                   // Recipient (The tagged user)
                                creator.getId(),                         // Sender (The post creator)
                                NotificationType.TAG_POST,      // Enum (Ensure you have this or similar)
                                notifMsg,                       // Message
                                savedPost.getId(),              // Reference ID (The Post ID)
                                "POST"                          // Reference Type
                        );
                    }
                }
            }

            List<MentionedUserDTO> mentionedUserDTOs = new ArrayList<>();
            if (mentionFriendIds != null && !mentionFriendIds.isEmpty()) {
                for (String mentionedUserId : mentionFriendIds) {
                    User mentionedUser = userRepository.findByUserId(mentionedUserId);
                    if (mentionedUser != null) {
                        PostMention postMention = PostMention.builder()
                                .post(savedPost)
                                .mentionedUser(mentionedUser)
                                .mentionedUserId(mentionedUserId)
                                .build();
                        postMentionRepository.save(postMention);

                        mentionedUserDTOs.add(MentionedUserDTO.builder()
                                .userId(mentionedUser.getUserId())
                                .username(mentionedUser.getUsername())
                                .build());

                        User MentionedUserDetails = userRepository.findByUserId(mentionedUserId);
                        User creator = userRepository.findByUserId(userId);

                        // ✅ NOTIFICATION: Send Mention Notification
                        String notifMsg = user.getUsername() + " mentioned you in a post.";
                        notificationService.createNotification(
                                MentionedUserDetails.getId(),                // Recipient
                                creator.getId(),                         // Sender
                                NotificationType.MENTION_POST,  // Enum (Ensure you have this or similar)
                                notifMsg,                       // Message
                                savedPost.getId(),              // Reference ID
                                "POST"                          // Reference Type
                        );
                    }
                }
            }

            List<MediaDTO> mediaDTOs = new ArrayList<>();
            if (files != null && files.length > 0) {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        Media media = processAndUploadFile(file, savedPost);
                        Media savedMedia = mediaRepository.save(media);

                        mediaDTOs.add(MediaDTO.builder()
                                .id(savedMedia.getId())
                                .bunnyUrl(savedMedia.getBunnyUrl())
                                .thumbnailUrl(savedMedia.getThumbnailUrl())
                                .mediaType(savedMedia.getMediaType().toString())
                                .build());
                    }
                }
            }

            PostResponseDTO response = PostResponseDTO.builder()
                    .id(savedPost.getId())
                    .content(savedPost.getContent())
                    .location(savedPost.getLocation())
                    .userId(savedPost.getUser().getUserId())
                    .username(savedPost.getUser().getUsername())
                    .createdAt(savedPost.getCreatedAt())
                    .shareCount(savedPost.getShareCount())
                    .mediaCount(mediaDTOs.size())
                    .likesCount(0)
                    .commentsCount(0)
                    .taggedUsers(taggedUserDTOs)
                    .mentionedUsers(mentionedUserDTOs)
                    .media(mediaDTOs)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to create post: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create Post", "message", e.getMessage()));
        }
    }


    private Media processAndUploadFile(MultipartFile file, Post post) throws IOException {
        String contentType = file.getContentType();
        BunnyNetUploadResult result;

        if (contentType != null && contentType.startsWith("image/")) {
            result = bunnyNetService.uploadImage(file);
//            result.setThumbnailUrl(result.getCdnUrl());
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

        media.setThumbnailUrl(result.getThumbnailUrl());

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
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }

            List<Post> posts = postRepository.findByUserOrderByCreatedAtDesc(user);

            List<PostResponseDTO> postDTOs = posts.stream()
                    .map(this::convertToPostDTO)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("posts", postDTOs);
            response.put("postCount", postDTOs.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to retrieve posts for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve posts", "message", e.getMessage()));
        }
    }

    private PostResponseDTO convertToPostDTO(Post post) {
        // Get tags
        List<TaggedUserDTO> taggedUsers = new ArrayList<>();
        if (post.getTags() != null) {
            taggedUsers = post.getTags().stream()
                    .map(tag -> TaggedUserDTO.builder()
                            .userId(tag.getTaggedUserId())
                            .username(tag.getTaggedUser() != null ? tag.getTaggedUser().getUsername() : null)
                            .build())
                    .collect(Collectors.toList());
        }

        List<MentionedUserDTO> mentionedUsers = new ArrayList<>();
        if (post.getMentions() != null) {
            mentionedUsers = post.getMentions().stream()
                    .map(mention -> MentionedUserDTO.builder()
                            .userId(mention.getMentionedUserId())
                            .username(mention.getMentionedUser() != null ? mention.getMentionedUser().getUsername() : null)
                            .build())
                    .collect(Collectors.toList());
        }


        List<MediaDTO> mediaDTOs = new ArrayList<>();
        if (post.getMedia() != null) {
            mediaDTOs = post.getMedia().stream()
                    .map(media -> MediaDTO.builder()
                            .id(media.getId())
                            .bunnyUrl(media.getBunnyUrl())
                            .thumbnailUrl(media.getThumbnailUrl())
                            .mediaType(media.getMediaType() != null ? media.getMediaType().toString() : null)
                            .build())
                    .collect(Collectors.toList());
        }

        return PostResponseDTO.builder()
                .id(post.getId())
                .content(post.getContent())
                .location(post.getLocation())
                .userId(post.getUser().getUserId())
                .username(post.getUser().getUsername())
                .createdAt(post.getCreatedAt())
                .shareCount(post.getShareCount())
                .mediaCount(mediaDTOs.size())
                .likesCount(post.getLikes() != null ? post.getLikes().size() : 0)
                .commentsCount(post.getComments() != null ? post.getComments().size() : 0)
                .taggedUsers(taggedUsers)
                .mentionedUsers(mentionedUsers)
                .media(mediaDTOs)
                .build();
    }


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

            if (!post.getUser().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You can only edit your own posts"));
            }

            post.setContent(content);
            post.setUpdatedAt(LocalDateTime.now());

            Post updatedPost = postRepository.save(post);

            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedPost.getId());
            response.put("content", updatedPost.getContent());
            response.put("updatedAt", updatedPost.getUpdatedAt());
            response.put("userId", updatedPost.getUser().getUserId()); // Safe access

            List<String> tagNames = updatedPost.getTags().stream()
                    .map(tag -> tag.getTaggedUser().getUsername()) // Assuming logic
                    .collect(Collectors.toList());
            response.put("tags", tagNames);

            return ResponseEntity.ok(response);

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

            List<Post> visiblePosts = postPage.getContent().stream()
                    .filter(post -> canViewPost(post, viewerId))
                    .collect(Collectors.toList());

            visiblePosts.forEach(post -> {
                categorizeUserId(post.getUser().getUserId(), allUserIds, personalUserIds, businessUserIds);

                if (post.getTags() != null) {
                    post.getTags().forEach(tag -> {
                        // Use the string column to avoid Lazy Loading the User entity just for the ID
                        String taggedId = tag.getTaggedUserId();
                        if (taggedId != null) {
                            categorizeUserId(taggedId, allUserIds, personalUserIds, businessUserIds);
                        }
                    });
                }

                if (post.getMentions() != null) {
                    post.getMentions().forEach(mention -> {
                        String mentionedId = mention.getMentionedUserId();
                        if (mentionedId != null) {
                            categorizeUserId(mentionedId, allUserIds, personalUserIds, businessUserIds);
                        }
                    });
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

                        List<Map<String, Object>> taggedUsersList = new ArrayList<>();
                        if (post.getTags() != null) {
                            taggedUsersList = post.getTags().stream()
                                    .map(tag -> {
                                        String uid = tag.getTaggedUserId();
                                        User taggedUser = usersMap.get(uid);
                                        if (taggedUser != null) {
                                            return buildUserInfo(
                                                    taggedUser,
                                                    userDetailsMap.get(uid),
                                                    clubDetailsMap.get(uid)
                                            );
                                        }
                                        return null;
                                    })
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList());
                        }
                        postData.put("taggedUsers", taggedUsersList);

                        List<Map<String, Object>> mentionedUsersList = new ArrayList<>();
                        if (post.getMentions() != null) {
                            mentionedUsersList = post.getMentions().stream()
                                    .map(mention -> {
                                        String uid = mention.getMentionedUserId();
                                        User mentionedUser = usersMap.get(uid);
                                        if (mentionedUser != null) {
                                            return buildUserInfo(
                                                    mentionedUser,
                                                    userDetailsMap.get(uid),
                                                    clubDetailsMap.get(uid)
                                            );
                                        }
                                        return null;
                                    })
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList());
                        }
                        postData.put("mentionedUsers", mentionedUsersList);

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


    private void categorizeUserId(String userId, Set<String> allIds, Set<String> personalIds, Set<String> businessIds) {
        if (userId == null) return;

        allIds.add(userId);
        if (userId.startsWith("BS")) {
            businessIds.add(userId);
        } else {
            personalIds.add(userId);
        }
    }
    private boolean canViewPost(Post post, Long viewerId) {
        Long postOwnerId = post.getUser().getId();

        if (postOwnerId.equals(viewerId)) {
            return true;
        }

        UserSettings postOwnerSettings = userSettingsRepository
                .findByUserId(post.getUser().getId())
                .orElse(null);

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

                        UserDetails userDetails = userDetailsRepository.findByUserId(storyOwner.getUserId());
                        // Map user info
                        StoryUserDTO userDTO = StoryUserDTO.builder()
                                .id(storyOwner.getId())
                                .userId(storyOwner.getUserId())
                                .profilePicture(userDetails.getProfilePicture())
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

    @Override
    public ResponseEntity<?> getPostByPostId(Long postId){
        try {
            Post post = getPostById(postId);

            PostResponseDTO postDTO = convertToPostDTO(post);

            return ResponseEntity.ok(postDTO);

        } catch (Exception e) {
            log.error("Failed to retrieve post by ID {}: {}", postId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve Post", "message", e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> recordStoryView(Long storyId, String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            Long userId = extractUserIdFromToken(token);
            if (userId == null) {
                response.put("error", "Unauthorized");
                response.put("message", "Invalid Token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Check if view already exists (to avoid duplicates)
            if (storyViewRepository.existsByStoryIdAndUserId(storyId, userId)) {
                response.put("success", true);
                response.put("message", "Story already viewed");
                return ResponseEntity.ok(response);
            }

            // Save new view
            StoryView view = new StoryView();
            view.setStoryId(storyId);
            view.setUserId(userId);
            storyViewRepository.save(view);

            response.put("success", true);
            response.put("message", "Story view recorded successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Internal Server Error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // --- AUTOMATIC DELETION SCHEDULER ---
    // Runs every hour to delete views older than 24 hours
    @Scheduled(cron = "0 0 * * * *")
    public void deleteExpiredStoryViews() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        storyViewRepository.deleteOldViews(cutoffTime);
        System.out.println("Cleaned up story views older than: " + cutoffTime);
    }

    private Long extractUserIdFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) return null;
        try {
            String jwt = token.substring(7);
            String[] chunks = jwt.split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payload = new String(decoder.decode(chunks[1]));
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(payload);
            if (node.has("User-Id")) {
                return node.get("User-Id").asLong();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public ResponseEntity<?> getStoryViewers(Long storyId) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<StoryView> views = storyViewRepository.findByStoryId(storyId);
            long viewCount = views.size();

            List<UserDetails> viewerProfiles = new ArrayList<>();

            if (!views.isEmpty()) {
                List<Long> dbIds = views.stream()
                        .map(StoryView::getUserId)
                        .distinct() // Remove duplicates just in case
                        .collect(Collectors.toList());

                List<User> users = userRepository.findAllById(dbIds);

                List<String> customUserIds = users.stream()
                        .map(User::getUserId)
                        .collect(Collectors.toList());

                if (!customUserIds.isEmpty()) {
                    viewerProfiles = userDetailsRepository.findByUserIdIn(customUserIds);
                }
            }

            response.put("success", HttpStatus.OK.value());
            response.put("total_views", viewCount);
            response.put("viewers", viewerProfiles);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to fetch story viewers");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @Override
    public ResponseEntity<?> savePost(String currentUserId, Long postId) {
        try {
            Optional<User> user = userRepository.findByEmail(currentUserId);
            Optional<Post> postOpt = postRepository.findById(postId);

            if (user == null || postOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User or Post not found.");
            }

            Post post = postOpt.get();

            if (savedPostRepository.existsByUserAndPost(user.get(), post)) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Post is already saved.");
                response.put("status", HttpStatus.CONFLICT.value());
                return ResponseEntity.ok(response);
            }

            SavedPost savedPost = new SavedPost(user.get(), post);
            savedPostRepository.save(savedPost);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Post saved successfully.");
            response.put("status", HttpStatus.OK.value());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving post.");
        }
    }

    @Override
    public ResponseEntity<?> removeSavedPost(String currentUserId, Long postId) {
        try {
            Optional<User> user = userRepository.findByEmail(currentUserId);
            Optional<Post> postOpt = postRepository.findById(postId);

            if (user == null || postOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User or Post not found.");
            }

            Optional<SavedPost> savedPostOpt = savedPostRepository.findByUserAndPost(user.get(), postOpt.get());

            if (savedPostOpt.isPresent()) {
                savedPostRepository.delete(savedPostOpt.get());
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Post removed from saved list.");
                response.put("status", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Post was not in saved list.");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error removing saved post.");
        }
    }

    @Override
    public ResponseEntity<?> getSavedPosts(String currentUserId, int page, int size) {
        try {
            Optional<User> user = userRepository.findByEmail(currentUserId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<SavedPost> savedPostPage = savedPostRepository.findByUserOrderBySavedAtDesc(user.get(), pageable);

            List<Map<String, Object>> responseList = new ArrayList<>();

            for (SavedPost savedEntry : savedPostPage.getContent()) {
                Post post = savedEntry.getPost();

                Map<String, Object> postMap = new HashMap<>();
                postMap.put("savedAt", savedEntry.getSavedAt());

                postMap.put("postId", post.getId());
                postMap.put("content", post.getContent());
                postMap.put("createdAt", post.getCreatedAt());
                postMap.put("shareCount", post.getShareCount());
                postMap.put("location", post.getLocation());

                if (post.getUser() != null) {
                    Map<String, Object> ownerMap = new HashMap<>();
                    ownerMap.put("userId", post.getUser().getUserId());
                    ownerMap.put("username", post.getUser().getUsername());
                    // Add profile picture logic here if available
                    postMap.put("postedBy", ownerMap);
                }
                if (post.getMedia() != null && !post.getMedia().isEmpty()) {
                    // Assuming Media entity has a getUrl() method
                    postMap.put("media", post.getMedia());
                }

                responseList.add(postMap);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("savedPosts", responseList);
            response.put("totalPages", savedPostPage.getTotalPages());
            response.put("totalElements", savedPostPage.getTotalElements());
            response.put("currentPage", savedPostPage.getNumber());
            response.put("status", HttpStatus.OK.value());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching saved posts.");
        }
    }


}