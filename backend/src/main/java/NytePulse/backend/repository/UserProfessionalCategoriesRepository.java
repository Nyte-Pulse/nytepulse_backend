package NytePulse.backend.repository;

import NytePulse.backend.entity.UserProfessionalCategories;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserProfessionalCategoriesRepository extends JpaRepository<UserProfessionalCategories,Long> {
    List<UserProfessionalCategories> findByUserId(Long userId);
}
