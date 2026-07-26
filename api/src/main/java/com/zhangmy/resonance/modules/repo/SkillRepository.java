package com.zhangmy.resonance.modules.repo;

import com.zhangmy.resonance.modules.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findByAdminIdAndVisibleTrueOrderBySortOrderAscIdAsc(Long adminId);

    List<Skill> findByAdminIdOrderBySortOrderAscIdAsc(Long adminId);

    List<Skill> findByAdminIdAndCategoryAndVisibleTrueOrderBySortOrderAsc(Long adminId, String category);

    List<Skill> findByAdminIdAndCategoryOrderBySortOrderAsc(Long adminId, String category);

    Optional<Skill> findByAdminIdAndName(Long adminId, String name);
}
