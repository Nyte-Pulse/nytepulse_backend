package NytePulse.backend.service.impl;

import NytePulse.backend.dto.LikeResponseDTO;
import NytePulse.backend.dto.PostStatsDTO;
import NytePulse.backend.entity.Post;
import NytePulse.backend.entity.PostLike;
import NytePulse.backend.entity.User;
import NytePulse.backend.entity.UserDetails;
import NytePulse.backend.repository.*;
import NytePulse.backend.service.centralServices.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostLikeServiceImpl implements PostLikeService {

    @Autowired
    private  PostLikeRepository postLikeRepository;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

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
    public ResponseEntity<?> getLikeCount(Long postId,String token) {

        try {
            if (!postRepository.existsById(postId)) {
                throw new RuntimeException("Post not found with id: " + postId);
            }

            Long totalLikes = postLikeRepository.countByPostId(postId);
            boolean isLiked = false;
            Long userId = extractUserIdFromToken(token);

            if (userId != null) {
                isLiked = postLikeRepository.existsByPostIdAndUserId(postId, userId);
            }

            LikeResponseDTO responseDTO = new LikeResponseDTO();
            responseDTO.setTotalLikes(totalLikes);
            responseDTO.setLiked(isLiked); // This sets the boolean
            responseDTO.setMessage("Total likes retrieved successfully");

            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get Like Count");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private Long extractUserIdFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }

        try {
            String jwt = token.substring(7);

            String[] chunks = jwt.split("\\.");
            if (chunks.length < 2) return null;

            java.util.Base64.Decoder decoder = java.util.Base64.getUrlDecoder();
            String payload = new String(decoder.decode(chunks[1]));

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(payload);

            if (node.has("User-Id")) {
                return node.get("User-Id").asLong();
            }
            return null;

        } catch (Exception e) {
            System.err.println("Token parsing failed: " + e.getMessage());
            return null;
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

    @Override
    public ResponseEntity<?> getLikedUsersByPostId(Long postId,int page,int size) {
        try{
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

            Page<PostLike> likesPage = postLikeRepository.findByPostId(postId, pageable);

            List<String> userIds = likesPage.getContent().stream()
                    .map(like -> like.getUser().getUserId())
                    .collect(Collectors.toList());

            List<UserDetails> userDetailsList = userDetailsRepository.findByUserIdIn(userIds);

            Page<UserDetails> resultPage = new PageImpl<>(
                    userDetailsList,
                    pageable,
                    likesPage.getTotalElements()
            );

            HashMap<String, Object> response = new HashMap<>();
            response.put("message", "Liked users retrieved successfully");
            response.put("status", HttpStatus.OK.value());
            response.put("result", resultPage);

            return ResponseEntity.ok(response);
        }catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get Liked Users By Post Id");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}