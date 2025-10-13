package NytePulse.backend.service.impl;

import NytePulse.backend.dto.LikeResponseDTO;
import NytePulse.backend.dto.PostStatsDTO;
import NytePulse.backend.entity.Post;
import NytePulse.backend.entity.PostLike;
import NytePulse.backend.entity.User;
import NytePulse.backend.repository.CommentRepository;
import NytePulse.backend.repository.PostLikeRepository;
import NytePulse.backend.repository.PostRepository;
import NytePulse.backend.repository.UserRepository;
import NytePulse.backend.service.centralServices.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostLikeServiceImpl implements PostLikeService {

    @Autowired
    private  PostLikeRepository postLikeRepository;

    @Autowired
    private  PostRepository postRepository;

    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private  CommentRepository commentRepository;

    @Override
    @Transactional
    public ResponseEntity<?> toggleLike(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Optional<PostLike> existingLike = postLikeRepository.findByPostIdAndUserId(postId, userId);

        boolean liked;
        String message;

        if (existingLike.isPresent()) {
            // Unlike the post
            postLikeRepository.delete(existingLike.get());
            liked = false;
            message = "Post unliked successfully";
        } else {
            // Like the post
            PostLike postLike = new PostLike();
            postLike.setPost(post);
            postLike.setUser(user);
            postLikeRepository.save(postLike);
            liked = true;
            message = "Post liked successfully";
        }

        Long totalLikes = postLikeRepository.countByPostId(postId);

        LikeResponseDTO response = new LikeResponseDTO();
        response.setLiked(liked);
        response.setTotalLikes(totalLikes);
        response.setMessage(message);

        return ResponseEntity.ok(response);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getLikeCount(Long postId) {

        try{
        if (!postRepository.existsById(postId)) {
            throw new RuntimeException("Post not found with id: " + postId);
        }

        Long totalLikes = postLikeRepository.countByPostId(postId);

        LikeResponseDTO responseDTO =new  LikeResponseDTO();
        responseDTO.setTotalLikes(totalLikes);
        responseDTO.setMessage("Total likes retrieved successfully");
        return ResponseEntity.ok(responseDTO);
    } catch (Exception e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Failed to get Like Count");
        errorResponse.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> isPostLikedByUser(Long postId, Long userId) {

        try{
        if (!postRepository.existsById(postId)) {
            throw new RuntimeException("Post not found with id: " + postId);
        }

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }

        boolean liked = postLikeRepository.existsByPostIdAndUserId(postId, userId);

        LikeResponseDTO responseDTO =new  LikeResponseDTO();
        responseDTO.setLiked(liked);
        responseDTO.setTotalLikes(0L); // Not applicable here
        responseDTO.setMessage("Like status retrieved successfully");
        return ResponseEntity.ok(responseDTO);
    } catch (Exception e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Failed to Post Liked By User");
        errorResponse.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPostStats(Long postId, Long userId) {
        try{
        if (!postRepository.existsById(postId)) {
            throw new RuntimeException("Post not found with id: " + postId);
        }

        Long totalLikes = postLikeRepository.countByPostId(postId);
        Long totalComments = commentRepository.countByPostId(postId);
        boolean likedByCurrentUser = postLikeRepository.existsByPostIdAndUserId(postId, userId);

        PostStatsDTO responseDTO = new PostStatsDTO();
        responseDTO.setTotalLikes(totalLikes);
        responseDTO.setTotalComments(totalComments);
        responseDTO.setLikedByCurrentUser(likedByCurrentUser);
        return ResponseEntity.ok(responseDTO);
    } catch (Exception e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Failed to get Post Status");
        errorResponse.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    }
}