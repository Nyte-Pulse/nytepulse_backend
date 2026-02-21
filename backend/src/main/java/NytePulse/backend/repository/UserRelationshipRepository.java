package NytePulse.backend.repository;

import NytePulse.backend.entity.User;
import NytePulse.backend.entity.UserRelationship;
import NytePulse.backend.entity.RelationshipType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    // In RelationshipRepository.java
    @Query("SELECT r.follower.id FROM UserRelationship r WHERE r.follower.id = :currentUserId AND r.follower.id IN :targetIds")
    Set<String> findFolloweeIdsByFollowerAndTargets(@Param("currentUserId") String currentUserId, @Param("targetIds") List<String> targetIds);

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
    long countFollowers(@Param("userId") Long userId);

    // Count following
    @Query("SELECT COUNT(ur) FROM UserRelationship ur " +
            "WHERE ur.follower.userId = :userId " +
            "AND ur.relationshipType = 'FOLLOWING'")
    long countFollowing(@Param("userId") Long userId);

    // Find relationship between two users using userId (String)
    @Query("SELECT ur FROM UserRelationship ur " +
            "WHERE ur.follower.userId = :followerUserId " +
            "AND ur.following.userId = :followingUserId")
    Optional<UserRelationship> findByFollowerUserIdAndFollowingUserId(
            @Param("followerUserId") String followerUserId,
            @Param("followingUserId") String followingUserId);

    // Find relationship using internal Long IDs
    Optional<UserRelationship> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

//    boolean existsByFollower_UserIdAndFollowing_UserId(String followerUserId, String followingUserId);

    // Alternative: Use @Query if the above doesn't work
    @Query("SELECT CASE WHEN COUNT(ur) > 0 THEN true ELSE false END " +
            "FROM UserRelationship ur " +
            "WHERE ur.follower.userId = :followerUserId " +
            "AND ur.following.userId = :followingUserId")
    boolean checkRelationshipExists(@Param("followerUserId") String followerUserId,
                                    @Param("followingUserId") String followingUserId);


    boolean existsByFollower_UserIdAndFollowing_UserId(
            String followerId,
            String followingId
    );

    // Get all follower IDs for a user
    @Query("SELECT ur.follower.userId FROM UserRelationship ur " +
            "WHERE ur.following.id = :userId " +
            "AND ur.relationshipType = 'FOLLOWING'")
    List<String> findFollowerUserIdsByFollowingId(@Param("userId") Long userId);

    boolean existsByFollower_IdAndFollowing_Id(Long commenterUserId, Long postOwnerUserId);

    @Query("SELECT COUNT(ur) > 0 FROM UserRelationship ur " +
            "WHERE ur.follower.userId = :followerId " +
            "AND ur.following.userId = :followingId")
    boolean existsByFollowerAndFollowing(
            @Param("followerId") String followerId,
            @Param("followingId") String followingId
    );


    @Query(value = """
    SELECT 
        u.user_id as userId,
        COALESCE(ud.username, cd.username) as username,
        COALESCE(ud.name, cd.name) as name,
        COALESCE(ud.profile_picture, cd.profile_picture) as profilePicture,
        COUNT(DISTINCT ur2.follower_id) as mutualFriendsCount,
        COALESCE(ud.account_type, cd.account_type) as accountType
    FROM user_relationships ur1
    INNER JOIN user_relationships ur2 
        ON ur1.following_id = ur2.following_id
    INNER JOIN users u ON u.id = ur2.follower_id
    LEFT JOIN user_details ud ON ud.user_id = u.user_id
    LEFT JOIN club_details cd ON cd.user_id = u.user_id AND ud.user_id IS NULL
    WHERE ur1.follower_id = :currentUserId
        AND ur2.follower_id != :currentUserId
        AND ur2.follower_id NOT IN (
            SELECT ur3.following_id 
            FROM user_relationships ur3
            WHERE ur3.follower_id = :currentUserId
        )
        AND u.user_id IS NOT NULL
        AND (ud.status = 'active' OR cd.status = 'active' OR (ud.status IS NULL AND cd.status IS NULL))
    GROUP BY u.user_id, ud.username, cd.username, ud.name, cd.name, 
             ud.profile_picture, cd.profile_picture, ud.account_type, cd.account_type
    ORDER BY mutualFriendsCount DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<Object[]> findFriendSuggestionsByMutualFollowers(
            @Param("currentUserId") Long currentUserId,
            @Param("limit") int limit
    );

    @Query(value = """
    SELECT 
        u.user_id as userId,
        COALESCE(ud.username, cd.username) as username,
        COALESCE(ud.name, cd.name) as name,
        COALESCE(ud.profile_picture, cd.profile_picture) as profilePicture,
        COUNT(DISTINCT ur2.following_id) as mutualFriendsCount,
        COALESCE(ud.account_type, cd.account_type) as accountType
    FROM user_relationships ur1
    INNER JOIN user_relationships ur2 
        ON ur1.following_id = ur2.follower_id
    INNER JOIN users u ON u.id = ur2.following_id
    LEFT JOIN user_details ud ON ud.user_id = u.user_id
    LEFT JOIN club_details cd ON cd.user_id = u.user_id AND ud.user_id IS NULL
    WHERE ur1.follower_id = :currentUserId
        AND ur2.following_id != :currentUserId
        AND ur2.following_id NOT IN (
            SELECT ur3.following_id 
            FROM user_relationships ur3
            WHERE ur3.follower_id = :currentUserId
        )
        AND u.user_id IS NOT NULL
        AND (ud.status = 'active' OR cd.status = 'active' OR (ud.status IS NULL AND cd.status IS NULL))
    GROUP BY u.user_id, ud.username, cd.username, ud.name, cd.name, 
             ud.profile_picture, cd.profile_picture, ud.account_type, cd.account_type
    ORDER BY mutualFriendsCount DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<Object[]> findFriendSuggestionsFromFollowingNetwork(
            @Param("currentUserId") Long currentUserId,
            @Param("limit") int limit
    );

    @Query(value = """
    SELECT 
        u.user_id as userId,
        COALESCE(ud.username, cd.username) as username,
        COALESCE(ud.name, cd.name) as name,
        COALESCE(ud.profile_picture, cd.profile_picture) as profilePicture,
        COUNT(DISTINCT u.user_id) as mutualFriendsCount,
        COALESCE(ud.account_type, cd.account_type) as accountType
    FROM user_relationships ur1
    INNER JOIN user_relationships ur2 
        ON ur1.following_id = ur2.following_id
    INNER JOIN users u ON u.id = ur1.following_id
    LEFT JOIN user_details ud ON ud.user_id = u.user_id
    LEFT JOIN club_details cd ON cd.user_id = u.user_id AND ud.user_id IS NULL
    WHERE ur1.follower_id = :userId1
        AND ur2.follower_id = :userId2
        AND ur1.follower_id != ur2.follower_id
        AND u.user_id IS NOT NULL
        AND (ud.status = 'active' OR cd.status = 'active' OR (ud.status IS NULL AND cd.status IS NULL))
    GROUP BY u.user_id, ud.username, cd.username, ud.name, cd.name, 
             ud.profile_picture, cd.profile_picture, ud.account_type, cd.account_type
    """, nativeQuery = true)
    List<Object[]> findMutualFriends(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2
    );


    @Query("SELECT CASE WHEN COUNT(ur) > 0 THEN true ELSE false END " +
            "FROM UserRelationship ur " +
            "WHERE ur.following.userId = :followingUserId " +
            "AND ur.follower.userId = :followerUserId " +
            "AND ur.relationshipType = 'FOLLOWING'")
    Boolean isFollowers(@Param("followingUserId") String followingUserId,
                        @Param("followerUserId") String followerUserId);


    // Existing method to get the list of followers


    // NEW: Optimization to check "Am I following these people?" in one query
    @Query("SELECT r.following.id FROM UserRelationship r " +
            "WHERE r.follower.id = :currentUserId " +
            "AND r.following.id IN :targetIds " +
            "AND r.relationshipType = 'FOLLOWING'")
    Set<Long> findFollowingIdsByFollowerAndTargets(
            @Param("currentUserId") Long currentUserId,
            @Param("targetIds") List<Long> targetIds
    );

    Optional<UserRelationship> findByFollowerAndFollowing(User blocker, User blocked);

    @Query("SELECT CASE WHEN COUNT(ur) > 0 THEN true ELSE false END " +
            "FROM UserRelationship ur " +
            "WHERE ur.follower.userId = :targetUserId " +
            "AND ur.following.userId = :userId " +
            "AND ur.relationshipType = 'BLOCKED'")
    boolean isBlocked(@Param("targetUserId") String userId,
                        @Param("userId") String targetUserId);


    @Query("SELECT r.following.id, COUNT(r) as cnt " +
            "FROM UserRelationship r " +
            "GROUP BY r.following.id " +
            "ORDER BY cnt DESC")
    List<Object[]> findTopUsersByFollowersCount(Pageable pageable);

    @Query("SELECT ur FROM UserRelationship ur WHERE ur.follower.id = :blockerId AND ur.relationshipType = 'BLOCKED'")
    List<UserRelationship> findBlockedUsersByBlockerId(@Param("blockerId") Long blockerId);


    boolean existsByFollower_IdAndFollowing_IdAndRelationshipType(
            Long followerId,
            Long followingId,
            RelationshipType type
    );

    // Efficiently check if ANY block exists between two users (either direction)
    @Query("SELECT COUNT(r) > 0 FROM UserRelationship r " +
            "WHERE r.relationshipType = 'BLOCKED' " +
            "AND ( " +
            "  (r.follower.id = :userId1 AND r.following.id = :userId2) " +
            "  OR " +
            "  (r.follower.id = :userId2 AND r.following.id = :userId1) " +
            ")")
    boolean hasBlockedRelationship(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
