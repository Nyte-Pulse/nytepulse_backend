package NytePulse.backend.service.impl;

import NytePulse.backend.dto.CommentRequestDTO;
import NytePulse.backend.dto.CommentResponseDTO;
import NytePulse.backend.dto.UserBasicDTO;
import NytePulse.backend.entity.Comment;
import NytePulse.backend.entity.Post;
import NytePulse.backend.entity.User;
import NytePulse.backend.entity.UserDetails;
import NytePulse.backend.repository.*;
import NytePulse.backend.service.centralServices.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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

    @Override
    @Transactional
    public ResponseEntity<?> addComment(Long postId, Long userId, CommentRequestDTO commentRequestDTO) {
        try {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

            Comment comment = new Comment();
            comment.setContent(commentRequestDTO.getContent());
            comment.setPost(post);
            comment.setUser(user);

            Comment savedComment = commentRepository.save(comment);
            CommentResponseDTO responseDTO = mapToCommentResponseDTO(savedComment, userId);

            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to add comment to post");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
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

            // Safely set post ID
            if (comment.getPost() != null) {
                dto.setPostId(comment.getPost().getId());
            }

            // Safely set user information
            if (comment.getUser() != null) {
                UserBasicDTO userDto = new UserBasicDTO();
                userDto.setId(comment.getUser().getId());
                userDto.setUsername(comment.getUser().getUsername());

                // Safely get user details
                try {
                    UserDetails userDetails = userDetailsRepository.findByUsername(comment.getUser().getUsername());
                    if (userDetails != null) {
                        userDto.setName(userDetails.getName());
                    }
                } catch (Exception e) {
                    // Log error but don't fail the whole mapping
                    System.err.println("Error fetching user details: " + e.getMessage());
                }

                dto.setUser(userDto);
            }

            dto.setCreatedAt(comment.getCreatedAt());
            dto.setUpdatedAt(comment.getUpdatedAt());

            // Set parent comment ID if exists
            if (comment.getParentComment() != null) {
                dto.setParentCommentId(comment.getParentComment().getId());
            }

            // Set like information
            Long likeCount = commentLikeRepository.countByCommentId(comment.getId());
            dto.setLikeCount(likeCount != null ? likeCount : 0L);

            // Check if current user liked the comment
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
}
