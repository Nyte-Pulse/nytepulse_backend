package NytePulse.backend.repository;

import NytePulse.backend.entity.FeedBack;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<FeedBack, Long> {
    Page<FeedBack> findAllByOrderByCreatedAtDesc(Pageable pageable);
}