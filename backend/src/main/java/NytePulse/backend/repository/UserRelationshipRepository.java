package NytePulse.backend.repository;

import NytePulse.backend.entity.User;
import NytePulse.backend.entity.UserRelationship;
import NytePulse.backend.entity.RelationshipType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRelationshipRepository extends JpaRepository<UserRelationship, Long> {

    // Check if user A follows user B using userId (String)
    @Query("SELECT CASE WHEN COUNT(ur) > 0 THEN true ELSE false END " +
            "FROM UserRelationship ur " +
            "WHERE ur.follower.userId = :followerUserId " +
            "AND ur.following.userId = :followingUserId " +
            "AND ur.relationshipType = 'FOLLOWING'")
    boolean isFollowing(@Param("followerUserId") String followerUserId,
                        @Param("followingUserId") String followingUserId);

    // Get all followers of a user
    @Query("SELECT ur.follower FROM UserRelationship ur " +
            "WHERE ur.following.userId = :userId " +
            "AND ur.relationshipType = 'FOLLOWING'")
    Page<User> getFollowers(@Param("userId") String userId, Pageable pageable);

    // Get all users that a user is following
    @Query("SELECT ur.following FROM UserRelationship ur " +
            "WHERE ur.follower.userId = :userId " +
            "AND ur.relationshipType = 'FOLLOWING'")
    Page<User> getFollowing(@Param("userId") String userId, Pageable pageable);

    // Count followers
    @Query("SELECT COUNT(ur) FROM UserRelationship ur " +
            "WHERE ur.following.userId = :userId " +
            "AND ur.relationshipType = 'FOLLOWING'")
    long countFollowers(@Param("userId") String userId);

    // Count following
    @Query("SELECT COUNT(ur) FROM UserRelationship ur " +
            "WHERE ur.follower.userId = :userId " +
            "AND ur.relationshipType = 'FOLLOWING'")
    long countFollowing(@Param("userId") String userId);

    // Find relationship between two users using userId (String)
    @Query("SELECT ur FROM UserRelationship ur " +
            "WHERE ur.follower.userId = :followerUserId " +
            "AND ur.following.userId = :followingUserId")
    Optional<UserRelationship> findByFollowerUserIdAndFollowingUserId(
            @Param("followerUserId") String followerUserId,
            @Param("followingUserId") String followingUserId);

    // Find relationship using internal Long IDs
    Optional<UserRelationship> findByFollowerIdAndFollowingId(Long followerId, Long followingId);
}
