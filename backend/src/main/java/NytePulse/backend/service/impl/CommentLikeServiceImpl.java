package NytePulse.backend.service.impl;

import NytePulse.backend.dto.LikeResponseDTO;
import NytePulse.backend.entity.Comment;
import NytePulse.backend.entity.CommentLike;
import NytePulse.backend.entity.User;
import NytePulse.backend.repository.CommentLikeRepository;
import NytePulse.backend.repository.CommentRepository;
import NytePulse.backend.repository.UserRepository;
import NytePulse.backend.service.centralServices.CommentLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentLikeServiceImpl implements CommentLikeService {

    @Autowired
    private  CommentLikeRepository commentLikeRepository;

    @Autowired
    private  CommentRepository commentRepository;

    @Autowired
    private  UserRepository userRepository;

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

        Long totalLikes = commentLikeRepository.countByCommentId(commentId);

        LikeResponseDTO response = new LikeResponseDTO();
        response.setLiked(liked);
        response.setTotalLikes(totalLikes);
        response.setMessage(message);

        return ResponseEntity.ok(response);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getCommentLikeCount(Long commentId) {

        Long totalLikes = commentLikeRepository.countByCommentId(commentId);
        LikeResponseDTO response = new LikeResponseDTO();
        response.setTotalLikes(totalLikes);

        return ResponseEntity.ok(response);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCommentLikedByUser(Long commentId, Long userId) {
        return commentLikeRepository.existsByCommentIdAndUserId(commentId, userId);
    }
}
