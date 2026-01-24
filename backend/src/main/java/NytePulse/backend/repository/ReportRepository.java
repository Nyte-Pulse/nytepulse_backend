package NytePulse.backend.repository;

import NytePulse.backend.entity.Report;
import NytePulse.backend.enums.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByReportType(ReportType reportType);

    List<Report> findByReportedUserIsNotNull();

    List<Report> findByReportedPostIsNotNull();
}