package NytePulse.backend.service.impl;

import NytePulse.backend.dto.CommentRequestDTO;
import NytePulse.backend.dto.CommentResponseDTO;
import NytePulse.backend.dto.UserBasicDTO;
import NytePulse.backend.entity.*;
import NytePulse.backend.enums.CommentVisibility;
import NytePulse.backend.enums.StoryCommentVisibility;
import NytePulse.backend.exception.PermissionDeniedException;
import NytePulse.backend.exception.ResourceNotFoundException;
import NytePulse.backend.repository.*;
import NytePulse.backend.service.centralServices.CommentService;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    @Autowired
    private UserRelationshipRepository userRelationshipRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Override
    @Transactional
    public ResponseEntity<?> addComment(Long postId, Long userId, CommentRequestDTO commentRequestDTO) {
        try {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

            User commenter = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

            UserSettings postOwnerSettings = userSettingsRepository
                    .findByUserId(post.getUser().getId())
                    .orElse(null);


            if (!canCommentOnPost(postOwnerSettings, post.getUser().getUserId(), commenter.getId())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "You don't have permission to comment on this post");
                errorResponse.put("message", "Post owner has restricted comments");
                errorResponse.put("status", HttpStatus.FORBIDDEN.value());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }

            validateCommentContent(commentRequestDTO.getContent());

            Comment comment = Comment.builder()
                    .content(commentRequestDTO.getContent())
                    .post(post)
                    .story(null)
                    .user(commenter)
                    .createdAt(LocalDateTime.now())
                    .build();

            Comment savedComment = commentRepository.save(comment);
            CommentResponseDTO responseDTO = mapToCommentResponseDTO(savedComment, userId);

            return ResponseEntity.ok(responseDTO);

        } catch (ResourceNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Resource not found");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (PermissionDeniedException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Permission denied");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (ValidationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid comment");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error adding comment to post {} by user {}: {}",
                    postId, userId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to add comment to post");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private boolean canCommentOnPost(UserSettings postOwnerSettings, String postOwnerUserId, Long commenterUserId) {
        if (postOwnerSettings == null) {
            // Default to followers-only if no settings
            return userRelationshipRepository.existsByFollower_IdAndFollowing_UserId(
                    commenterUserId, postOwnerUserId);
        }

        CommentVisibility commentVisibility = postOwnerSettings.getCommentVisibility();

        System.out.println("Post owner comment visibility: " + commentVisibility);

        switch (commentVisibility) {
            case EVERYONE:
                return true;
            case FOLLOWERS:
                return userRelationshipRepository.existsByFollower_IdAndFollowing_UserId(
                        commenterUserId, postOwnerUserId);
            case MENTIONED_ONLY:
                // Check if commenter is mentioned in post content
                return isMentionedInPost(postOwnerUserId, commenterUserId);
            case DISABLED:
                return false;
            default:
                return false;
        }
    }

    private boolean isMentionedInPost(String postOwnerUserId, Long commenterUserId) {
        // Implementation depends on your mention format
        // Example: Check if @username exists in post content
        return false; // Placeholder
    }

    private void validateCommentContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new ValidationException("Comment content cannot be empty");
        }

        if (content.length() > 1000) {
            throw new ValidationException("Comment too long (max 1000 characters)");
        }

        // Add profanity filter, spam check, etc.
//        if (containsProfanity(content)) {
//            throw new ValidationException("Content violates community guidelines");
//        }
    }


    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getCommentsByPostId(Long postId) {
        try {
            if (!postRepository.existsById(postId)) {
                throw new RuntimeException("Post not found with id: " + postId);
            }

            List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtDesc(postId);

            List<CommentResponseDTO> responseDTOs = comments.stream()
                    .map(comment -> mapToCommentResponseDTO(comment, null))
                    .filter(dto -> dto != null) // Filter out null results
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responseDTOs);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get comments by post Id");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateComment(Long commentId, Long userId, CommentRequestDTO commentRequestDTO) {
        try {
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

            if (!comment.getUser().getId().equals(userId)) {
                throw new RuntimeException("You are not authorized to update this comment");
            }

            comment.setContent(commentRequestDTO.getContent());
            Comment updatedComment = commentRepository.save(comment);

            CommentResponseDTO responseDTO = mapToCommentResponseDTO(updatedComment, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("comment", responseDTO);
            response.put("message", "Comment updated successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update comment");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteComment(Long commentId, Long userId) {
        try {
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

            if (!comment.getUser().getId().equals(userId)) {
                throw new RuntimeException("You are not authorized to delete this comment");
            }

            commentRepository.delete(comment);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Comment deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete comment");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getCommentCount(Long postId) {
        try {
            Long count = commentRepository.countByPostId(postId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get comment count");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> addReply(Long commentId, Long userId, CommentRequestDTO commentRequestDTO) {
        try {
            Comment parentComment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found with id: " + commentId));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

            Comment reply = new Comment();
            reply.setContent(commentRequestDTO.getContent());
            reply.setPost(parentComment.getPost());
            reply.setUser(user);
            reply.setParentComment(parentComment);

            Comment savedReply = commentRepository.save(reply);

            CommentResponseDTO responseDTO = mapToCommentResponseDTO(savedReply, userId);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to add reply");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getCommentsWithRepliesByPostId(Long postId, Long currentUserId) {
        try {
            if (!postRepository.existsById(postId)) {
                throw new RuntimeException("Post not found with id: " + postId);
            }

            List<Comment> rootComments = commentRepository.findRootCommentsByPostId(postId);

            List<CommentResponseDTO> responseDTOs = rootComments.stream()
                    .map(comment -> mapToCommentResponseDTOWithReplies(comment, currentUserId))
                    .filter(dto -> dto != null) // Filter out null results
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responseDTOs);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get comments with replies");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private CommentResponseDTO mapToCommentResponseDTO(Comment comment, Long currentUserId) {
        if (comment == null) {
            return null;
        }

        try {
            CommentResponseDTO dto = new CommentResponseDTO();
            dto.setId(comment.getId());
            dto.setContent(comment.getContent());

            if (comment.getPost() != null) {
                dto.setPostId(comment.getPost().getId());
            }

            if (comment.getUser() != null) {
                UserBasicDTO userDto = new UserBasicDTO();
                userDto.setId(comment.getUser().getId());
                userDto.setUsername(comment.getUser().getUsername());

                try {
                    UserDetails userDetails = userDetailsRepository.findByUsername(comment.getUser().getUsername());
                    if (userDetails != null) {
                        userDto.setName(userDetails.getName());
                        userDto.setProfilePicture(userDetails != null ? userDetails.getProfilePicture() : null);
                    }
                } catch (Exception e) {
                    // Log error but don't fail the whole mapping
                    System.err.println("Error fetching user details: " + e.getMessage());
                }

                dto.setUser(userDto);
            }

            dto.setCreatedAt(comment.getCreatedAt());
            dto.setUpdatedAt(comment.getUpdatedAt());

            if (comment.getParentComment() != null) {
                dto.setParentCommentId(comment.getParentComment().getId());
            }

            Long likeCount = commentLikeRepository.countByCommentId(comment.getId());
            dto.setLikeCount(likeCount != null ? likeCount : 0L);

            boolean likedByCurrentUser = false;
            if (currentUserId != null) {
                likedByCurrentUser = commentLikeRepository.existsByCommentIdAndUserId(comment.getId(), currentUserId);
            }
            dto.setLikedByCurrentUser(likedByCurrentUser);

            // Get reply count from database
            Long replyCount = commentRepository.countByParentCommentId(comment.getId());
            dto.setReplyCount(replyCount != null ? replyCount : 0L);

            // Initialize empty replies list (will be populated by withReplies method)
            dto.setReplies(new ArrayList<>());

            return dto;

        } catch (Exception e) {
            System.err.println("Error mapping comment to DTO: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private CommentResponseDTO mapToCommentResponseDTOWithReplies(Comment comment, Long currentUserId) {
        if (comment == null) {
            return null;
        }

        // First map the comment itself
        CommentResponseDTO dto = mapToCommentResponseDTO(comment, currentUserId);

        if (dto == null) {
            return null;
        }

        try {
            // Then fetch and recursively map all replies
            List<Comment> replies = commentRepository.findByParentCommentIdOrderByCreatedAtAsc(comment.getId());

            if (replies != null && !replies.isEmpty()) {
                List<CommentResponseDTO> replyDTOs = replies.stream()
                        .map(reply -> mapToCommentResponseDTOWithReplies(reply, currentUserId))
                        .filter(replyDto -> replyDto != null) // Filter out null results
                        .collect(Collectors.toList());

                dto.setReplies(replyDTOs);
            } else {
                dto.setReplies(new ArrayList<>());
            }
        } catch (Exception e) {
            System.err.println("Error fetching replies for comment " + comment.getId() + ": " + e.getMessage());
            dto.setReplies(new ArrayList<>());
        }

        return dto;
    }

    @Override
    public ResponseEntity<?> addCommentToStory(Long storyId, Long userId, CommentRequestDTO commentRequestDTO) {
        try {
            Story story = storyRepository.findStoryById(storyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Story not found with id: " + storyId));

            User commenter = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

            UserSettings storyOwnerSettings = userSettingsRepository
                    .findByUserId(story.getUser().getId())
                    .orElse(null);

            if (!canCommentOnStory(storyOwnerSettings, story.getUser().getId(), userId)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Permission denied");
                errorResponse.put("message", "You don't have permission to comment on this story");
                errorResponse.put("status", HttpStatus.FORBIDDEN.value());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }

            validateCommentContent(commentRequestDTO.getContent());

            Comment comment = Comment.builder()
                    .content(commentRequestDTO.getContent())
                    .story(story)
                    .post(null)
                    .user(commenter)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Comment savedComment = commentRepository.save(comment);
            CommentResponseDTO responseDTO = mapToCommentResponseDTO(savedComment, userId);

            return ResponseEntity.ok(responseDTO);

        } catch (ResourceNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Resource not found");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (PermissionDeniedException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Permission denied");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (ValidationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid comment");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error adding comment to story {} by user {}: {}",
                    storyId, userId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to add comment to story");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private boolean canCommentOnStory(UserSettings storyOwnerSettings, Long storyOwnerId, Long commenterId) {

        if (storyOwnerId.equals(commenterId)) {
            return true;
        }

        if (storyOwnerSettings == null) {
            return isFollowing(commenterId, storyOwnerId);
        }

        StoryCommentVisibility storyCommentVisibility = storyOwnerSettings.getStoryCommentVisibility();

        switch (storyCommentVisibility) {
            case EVERYONE:
                return true;

            case FOLLOWERS:
                return isFollowing(commenterId, storyOwnerId);

            case MENTIONED_ONLY:
                // For stories, treat as FOLLOWERS or implement mention logic
                return isFollowing(commenterId, storyOwnerId);

            case DISABLED:
                return false;

            default:
                return false;
        }
    }

    private boolean isFollowing(Long followerId, Long followingId) {
        return userRelationshipRepository.existsByFollower_IdAndFollowing_Id(followerId, followingId);
    }


}
