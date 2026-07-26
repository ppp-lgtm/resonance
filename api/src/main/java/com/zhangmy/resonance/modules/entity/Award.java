package com.zhangmy.resonance.modules.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "award", indexes = {
        @Index(name = "idx_award_admin_date", columnList = "admin_id,award_date DESC")
})
@Getter
@Setter
public class Award implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "issuer", length = 255, nullable = false)
    private String issuer;

    @Column(name = "award_date", nullable = false)
    private LocalDate awardDate;

    @Lob
    @Column(name = "description_", columnDefinition = "TEXT")
    private String description;

    @Column(name = "certificate_url", length = 1024)
    private String certificateUrl;

    /** 奖项封面图 URL（奖状照片/奖杯图等） */
    @Column(name = "cover_url", length = 1024)
    private String coverUrl;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
