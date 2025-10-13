package NytePulse.backend.repository;

import NytePulse.backend.entity.ReportEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportEventRepository extends JpaRepository<ReportEvent, Long> {

    ReportEvent findByEventIdAndReporterId(String eventId, String reporterId);
}
