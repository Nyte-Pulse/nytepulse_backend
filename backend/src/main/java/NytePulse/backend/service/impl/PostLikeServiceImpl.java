package NytePulse.backend.service.impl;

import NytePulse.backend.dto.LikeResponseDTO;
import NytePulse.backend.dto.LikedUserDTO;
import NytePulse.backend.dto.PostStatsDTO;
import NytePulse.backend.entity.Post;
import NytePulse.backend.entity.PostLike;
import NytePulse.backend.entity.User;
import NytePulse.backend.entity.UserDetails;
import NytePulse.backend.enums.ReactionType;
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
import java.util.function.Function;
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
    public ResponseEntity<?> toggleLike(Long postId, Long userId,String reactionTypeString) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        ReactionType incomingReaction;
        try {
            incomingReaction = ReactionType.valueOf(reactionTypeString.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new RuntimeException("Invalid reaction type: " + reactionTypeString);
        }

        Optional<PostLike> existingLikeOpt = postLikeRepository.findByPostIdAndUserId(postId, userId);

        boolean isReacted;
        String currentReactionName = null;
        String message;

        if (existingLikeOpt.isPresent()) {
            PostLike existingLike = existingLikeOpt.get();

            if (existingLike.getReactionType() == incomingReaction) {
                // SCENARIO A: Same reaction clicked again -> Remove it (Toggle OFF)
                postLikeRepository.delete(existingLike);
                isReacted = false;
                message = "Reaction removed successfully";
            } else {
                // SCENARIO B: Different reaction clicked -> Update it
                existingLike.setReactionType(incomingReaction);
                postLikeRepository.save(existingLike);

                isReacted = true;
                currentReactionName = incomingReaction.name();
                message = "Reaction updated to " + incomingReaction.name();
            }
        } else {
            // SCENARIO C: No reaction exists -> Create new one
            PostLike newLike = new PostLike();
            newLike.setPost(post);
            newLike.setUser(user);
            newLike.setReactionType(incomingReaction);

            postLikeRepository.save(newLike);

            isReacted = true;
            currentReactionName = incomingReaction.name();
            message = "Reacted with " + incomingReaction.name() + " successfully";
        }

        Long totalLikes = postLikeRepository.countByPostId(postId);

        LikeResponseDTO response = new LikeResponseDTO();
        response.setLiked(isReacted);
        response.setCurrentReaction(currentReactionName);
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
    public ResponseEntity<?> getLikedUsersByPostId(Long postId, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

            Page<PostLike> likesPage = postLikeRepository.findByPostId(postId, pageable);

            List<String> userIds = likesPage.getContent().stream()
                    .map(like -> like.getUser().getUserId())
                    .collect(Collectors.toList());

            List<UserDetails> userDetailsList = userDetailsRepository.findByUserIdIn(userIds);

            Map<String, UserDetails> userDetailsMap = userDetailsList.stream()
                    .collect(Collectors.toMap(UserDetails::getUserId, Function.identity()));

            List<LikedUserDTO> dtos = likesPage.getContent().stream().map(like -> {
                LikedUserDTO dto = new LikedUserDTO();

                UserDetails details = userDetailsMap.get(like.getUser().getUserId());

                if (details != null) {
                    dto.setUserDetailsId(details.getUserDetailsId());
                    dto.setUserId(details.getUserId());
                    dto.setUsername(details.getUsername());
                    dto.setName(details.getName());
                    dto.setProfilePicture(details.getProfilePicture());
                    dto.setBio(details.getBio());
                }

                if (like.getReactionType() != null) {
                    dto.setReactionType(like.getReactionType().name());
                }

                return dto;
            }).collect(Collectors.toList());

            Page<LikedUserDTO> resultPage = new PageImpl<>(
                    dtos,
                    pageable,
                    likesPage.getTotalElements()
            );

            HashMap<String, Object> response = new HashMap<>();
            response.put("message", "Liked users retrieved successfully");
            response.put("status", HttpStatus.OK.value());
            response.put("result", resultPage);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get Liked Users By Post Id");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}