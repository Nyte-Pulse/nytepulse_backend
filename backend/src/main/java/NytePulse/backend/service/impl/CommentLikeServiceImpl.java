package NytePulse.backend.service.impl;

import NytePulse.backend.dto.LikeResponseDTO;
import NytePulse.backend.entity.Comment;
import NytePulse.backend.entity.CommentLike;
import NytePulse.backend.entity.User;
import NytePulse.backend.enums.NotificationType;
import NytePulse.backend.repository.CommentLikeRepository;
import NytePulse.backend.repository.CommentRepository;
import NytePulse.backend.repository.UserRepository;
import NytePulse.backend.service.NotificationService;
import NytePulse.backend.service.centralServices.CommentLikeService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.PublicKey;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentLikeServiceImpl implements CommentLikeService {

    @Autowired
    private  CommentLikeRepository commentLikeRepository;

    @Autowired
    private  CommentRepository commentRepository;

    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public ResponseEntity<?> toggleCommentLike(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Optional<CommentLike> existingLike = commentLikeRepository.findByCommentIdAndUserId(commentId, userId);

        boolean liked;
        String message;

        if (existingLike.isPresent()) {
            commentLikeRepository.delete(existingLike.get());
            liked = false;
            message = "Comment unliked successfully";
        } else {
            CommentLike commentLike = new CommentLike();
            commentLike.setComment(comment);
            commentLike.setUser(user);
            commentLikeRepository.save(commentLike);
            liked = true;
            message = "Comment liked successfully";
        }

//        if (!comment.getUser().getId().equals(user.getId())) {
//            try {
//                String notifMsg = user.getUsername() + " liked your comment.";
//                notificationService.createNotification(
//                        comment.getUser().getId(),       // Recipient (Comment Owner)
//                        user.getId(),                    // Sender (Liker)
//                        NotificationType.LIKE_COMMENT,                   // Ensure this ENUM exists
//                        notifMsg,                                        // Message
//                        comment.getPost().getId(),       // Reference ID (Link to Post)
//                        "POST"                                           // Reference Type
//                );
//            } catch (Exception e) {
//                log.error("Failed to send comment like notification: {}", e.getMessage());
//            }
//        }


        Long totalLikes = commentLikeRepository.countByCommentId(commentId);

        LikeResponseDTO response = new LikeResponseDTO();
        response.setLiked(liked);
        response.setTotalLikes(totalLikes);
        response.setMessage(message);

        return ResponseEntity.ok(response);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<LikeResponseDTO> getCommentLikeCount(Long commentId, String token) {

        Long userId = null;

        if (token != null && token.startsWith("Bearer ")) {
            try {
                String jwt = token.substring(7);
                userId = extractUserIdFromToken(jwt);
            } catch (Exception e) {
                System.err.println("Failed to extract User ID from token: " + e.getMessage());
            }
        }

        Long totalLikes = commentLikeRepository.countByCommentId(commentId);

        boolean isLiked = false;
        if (userId != null) {
            isLiked = commentLikeRepository.existsByCommentIdAndUserId(commentId, userId);
        }

        LikeResponseDTO response = new LikeResponseDTO();
        response.setTotalLikes(totalLikes);
        response.setLiked(isLiked);
        response.setMessage("Like status fetched successfully");

        return ResponseEntity.ok(response);
    }

    private Long extractUserIdFromToken(String token) {
        try {
            String[] chunks = token.split("\\.");
            if (chunks.length < 2) return null;

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
    @Transactional(readOnly = true)
    public boolean isCommentLikedByUser(Long commentId, Long userId) {
        return commentLikeRepository.existsByCommentIdAndUserId(commentId, userId);
    }


    @Override
    public ResponseEntity<?> getLikedUsersByPostId(Long postId){

//        CommentLike commentLike=commentLikeRepository.findLikedUsersByPostId(postId);

        return ResponseEntity.ok(true);

    }
}
