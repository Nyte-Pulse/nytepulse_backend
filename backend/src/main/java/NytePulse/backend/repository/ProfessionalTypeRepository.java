package NytePulse.backend.repository;


import NytePulse.backend.entity.ProfessionalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfessionalTypeRepository extends JpaRepository<ProfessionalType, Long> {

    List<ProfessionalType> findByCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(Long categoryId);

    Optional<ProfessionalType> findByTypeCode(String typeCode);

    List<ProfessionalType> findByIsActiveTrueOrderByDisplayOrderAsc();
}
