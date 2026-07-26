package com.zhangmy.resonance.modules.repo;

import com.zhangmy.resonance.modules.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EducationRepository extends JpaRepository<Education, Long> {
    List<Education> findByAdminIdOrderByStartDateDescSortOrderAscIdDesc(Long adminId);
}
