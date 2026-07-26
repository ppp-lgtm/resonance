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
@Table(name = "contact_info", indexes = {
        @Index(name = "idx_contact_admin_visible_sort", columnList = "admin_id,is_visible,sort_order")
})
@Getter
@Setter
public class ContactInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(name = "platform", length = 32, nullable = false)
    private String platform;

    @Column(name = "icon", length = 64)
    private String icon;

    @Column(name = "value_", length = 512, nullable = false)
    private String value;

    @Column(name = "link", length = 1024)
    private String link;

    @Column(name = "copyable", nullable = false)
    private Boolean copyable = true;

    @Column(name = "is_visible", nullable = false)
    private Boolean visible = true;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
