package NytePulse.backend.repository;

import NytePulse.backend.entity.UserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDetailsRepository extends JpaRepository<UserDetails, Long> {

    UserDetails findByUserId(String userId);
    UserDetails findByEmail(String email);
    UserDetails findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    Page<UserDetails> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<UserDetails> findByAccountType(String accountType);

    @Query("SELECT ud FROM UserDetails ud " +
            "WHERE ud.userId IN :userIds " +
            "AND (LOWER(ud.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(ud.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY ud.name ASC")
    Page<UserDetails> searchByNameInUserIds(
            @Param("userIds") List<String> userIds,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );
}