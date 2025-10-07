package NytePulse.backend.service.impl;

import NytePulse.backend.dto.CommentRequestDTO;
import NytePulse.backend.dto.CommentResponseDTO;
import NytePulse.backend.dto.UserBasicDTO;
import NytePulse.backend.entity.Comment;
import NytePulse.backend.entity.Post;
import NytePulse.backend.entity.User;
import NytePulse.backend.entity.UserDetails;
import NytePulse.backend.repository.CommentRepository;
import NytePulse.backend.repository.PostRepository;
import NytePulse.backend.repository.UserDetailsRepository;
import NytePulse.backend.repository.UserRepository;
import NytePulse.backend.service.centralServices.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    @Autowired
    private  CommentRepository commentRepository;

    @Autowired
    private  PostRepository postRepository;

    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Override
    @Transactional
    public ResponseEntity<?> addComment(Long postId, Long userId, CommentRequestDTO commentRequestDTO) {
        try{
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Comment comment = new Comment();
        comment.setContent(commentRequestDTO.getContent());
        comment.setPost(post);
        comment.setUser(user);

        Comment savedComment = commentRepository.save(comment);
        CommentResponseDTO responseDTO = mapToCommentResponseDTO(savedComment);

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
        try{
        if (!postRepository.existsById(postId)) {
            throw new RuntimeException("Post not found with id: " + postId);
        }

        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtDesc(postId);

        List<CommentResponseDTO> responseDTOs = comments.stream()
                .map(this::mapToCommentResponseDTO)
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
        try{
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to update this comment");
        }

        comment.setContent(commentRequestDTO.getContent());
        Comment updatedComment = commentRepository.save(comment);

        CommentResponseDTO responseDTO = mapToCommentResponseDTO(updatedComment);

        Map<String, Object> response = new HashMap<>();
        response.put("User", responseDTO.getUser().getId());
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
        try{
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to delete this comment");
        }

        commentRepository.delete(comment);
        return ResponseEntity.ok("Comment deleted successfully");
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

        Long count = commentRepository.countByPostId(postId);
        return ResponseEntity.ok(count);

    }

    private CommentResponseDTO mapToCommentResponseDTO(Comment comment) {
        try{

        UserDetails userDetails=userDetailsRepository.findByUsername(comment.getUser().getUsername());

        CommentResponseDTO dto = new CommentResponseDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setPostId(comment.getPost().getId());

        UserBasicDTO userDto = new UserBasicDTO();
        userDto.setId(comment.getUser().getId());
        userDto.setUsername(comment.getUser().getUsername());
        userDto.setName(userDetails.getName());

        dto.setUser(userDto);
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());

        return dto;
    } catch (Exception e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Failed to map To Comment Response DTO");
        errorResponse.put("message", e.getMessage());
        return null;
    }
    }

}