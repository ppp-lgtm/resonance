package com.zhangmy.resonance.modules.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project", indexes = {
        @Index(name = "idx_project_admin_published_sort", columnList = "admin_id,is_published,sort_order"),
        @Index(name = "idx_project_featured", columnList = "admin_id,is_featured")
})
@Getter
@Setter
public class Project implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "summary", length = 500, nullable = false)
    private String summary;

    @Lob
    @Column(name = "description_", columnDefinition = "MEDIUMTEXT", nullable = false)
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags_json", columnDefinition = "json", nullable = false)
    private List<String> tags = new ArrayList<>();

    @Column(name = "github_url", length = 512)
    private String githubUrl;

    @Column(name = "demo_url", length = 512)
    private String demoUrl;

    @Column(name = "video_url", length = 512)
    private String videoUrl;

    @Column(name = "is_featured", nullable = false)
    private Boolean featured = false;

    @Column(name = "is_published", nullable = false)
    private Boolean published = true;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /** 项目状态：ONLINE(已上线) / PREPARING(筹备中) / PLANNING(规划中) */
    @Enumerated(EnumType.STRING)
    @Column(name = "status_", length = 16)
    private ProjectStatus status;

    /** 完成年份（4 位数字，手输，允许空） */
    @Column(name = "completion_year")
    private Integer completionYear;

    /**
     * 项目概述正文（前台详情弹窗左侧 italic 大段引用）。
     * 为空时前台回退显示原 description（兼容旧数据）。
     */
    @Lob
    @Column(name = "overview", columnDefinition = "TEXT")
    private String overview;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /* =============== 多图（项目图片）= 非 owning 端，由 ProjectImage 维护外键 =============== */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC, id ASC")
    private List<ProjectImage> images = new ArrayList<>();

    /** 便利方法：同步设置多图（替换） */
    public void replaceImages(List<ProjectImage> newImages) {
        this.images.clear();
        if (newImages != null) {
            for (ProjectImage img : newImages) {
                img.setProject(this);
                this.images.add(img);
            }
        }
    }
}
