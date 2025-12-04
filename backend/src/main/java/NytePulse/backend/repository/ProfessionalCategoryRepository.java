package NytePulse.backend.repository;


import NytePulse.backend.entity.ProfessionalCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfessionalCategoryRepository extends JpaRepository<ProfessionalCategory, Long> {

    Optional<ProfessionalCategory> findByCategoryCode(String categoryCode);

    List<ProfessionalCategory> findByIsActiveTrueOrderByDisplayOrderAsc();

    @Query("SELECT c FROM ProfessionalCategory c LEFT JOIN FETCH c.professionalTypes WHERE c.isActive = true ORDER BY c.displayOrder")
    List<ProfessionalCategory> findAllActiveWithTypes();
}
