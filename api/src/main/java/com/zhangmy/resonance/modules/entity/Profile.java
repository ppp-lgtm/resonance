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
@Table(name = "profile")
@Getter
@Setter
public class Profile implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false, unique = true)
    private Long adminId;

    @Column(name = "name", length = 64, nullable = false)
    private String name;

    /** 头衔数组 JSON */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "titles_json", columnDefinition = "json", nullable = false)
    private List<String> titles = new ArrayList<>();

    @Column(name = "slogan", length = 200, nullable = false)
    private String slogan;

    @Lob
    @Column(name = "bio", columnDefinition = "MEDIUMTEXT", nullable = false)
    private String bio;

    /** 所在城市 / 模块所在地（约 2021 中 FACT_GROUPS 第一组） */
    @Column(name = "location", length = 128)
    private String location;

    /** 从业经验（年数） */
    @Column(name = "years_experience")
    private Integer yearsExperience;

    /** 专注方向 / 服务模块数组 JSON（对应前台 About 4 卡片「服务设计 / 产品工程 / 团队协作 / 持续学习」） */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "focus_areas_json", columnDefinition = "json")
    private List<String> focusAreas = new ArrayList<>();

    /** 合作模式（如：远程 / 驻场 / 咨询） */
    @Column(name = "working_mode", length = 128)
    private String workingMode;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(name = "resume_url", length = 512)
    private String resumeUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
