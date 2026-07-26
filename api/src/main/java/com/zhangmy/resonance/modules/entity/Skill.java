package com.zhangmy.resonance.modules.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "skill", indexes = {
        @Index(name = "idx_skill_admin_cat_sort", columnList = "admin_id,category,sort_order"),
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_skill_admin_name", columnNames = {"admin_id", "name"})
})
@Getter
@Setter
public class Skill implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(name = "name", length = 64, nullable = false)
    private String name;

    @Column(name = "category", length = 32, nullable = false)
    private String category;

    @Column(name = "icon", length = 64)
    private String icon;

    @Column(name = "proficiency", nullable = false)
    private Integer proficiency;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "is_visible", nullable = false)
    private Boolean visible = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
