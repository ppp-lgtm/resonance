package com.zhangmy.resonance.modules.repo;

import com.zhangmy.resonance.modules.entity.Award;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AwardRepository extends JpaRepository<Award, Long> {
    List<Award> findByAdminIdOrderByAwardDateDescSortOrderAscIdDesc(Long adminId);
}
