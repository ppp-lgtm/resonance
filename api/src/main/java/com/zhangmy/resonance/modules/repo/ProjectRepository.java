package com.zhangmy.resonance.modules.repo;

import com.zhangmy.resonance.modules.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /** 后台列表：全部（含 images） */
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.images WHERE p.adminId = :adminId ORDER BY p.sortOrder ASC, p.id DESC")
    List<Project> findByAdminIdOrderBySortOrderAscCreatedAtDesc(@Param("adminId") Long adminId);

    /** 公开：仅已发布（含 images） */
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.images WHERE p.adminId = :adminId AND p.published = true ORDER BY p.sortOrder ASC, p.id DESC")
    List<Project> findByAdminIdAndPublishedTrueOrderBySortOrderAscIdDesc(@Param("adminId") Long adminId);

    /** 后台列表（含 images） */
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.images WHERE p.adminId = :adminId ORDER BY p.sortOrder ASC, p.id DESC")
    List<Project> listAdmin(@Param("adminId") Long adminId);

    /** 按 ID 取单个（含 images） */
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.images WHERE p.id = :id")
    java.util.Optional<Project> findDetailById(@Param("id") Long id);

    long countByAdminIdAndPublishedTrue(Long adminId);

    long countByAdminId(Long adminId);
}
