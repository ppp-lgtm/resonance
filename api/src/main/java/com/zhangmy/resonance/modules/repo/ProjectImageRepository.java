package com.zhangmy.resonance.modules.repo;

import com.zhangmy.resonance.modules.entity.ProjectImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectImageRepository extends JpaRepository<ProjectImage, Long> {
    List<ProjectImage> findByProjectIdOrderBySortOrderAscIdAsc(Long projectId);

    void deleteByProjectId(Long projectId);
}
