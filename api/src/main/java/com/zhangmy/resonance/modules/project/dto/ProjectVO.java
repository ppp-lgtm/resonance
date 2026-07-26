package com.zhangmy.resonance.modules.project.dto;

import com.zhangmy.resonance.modules.entity.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 列表卡片 + 详情通用 VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectVO implements Serializable {
    private Long id;
    private String title;
    private String summary;
    /** 列表视图：可以不返回 description（非 null 判断是否为空，按需截断） */
    private String description;
    /**
     * 项目概述正文（前台 italic 大字引用段）。
     * 为空时前端应 fallback 显示 description，兼容旧数据。
     */
    private String overview;
    private List<String> tags;
    /** 首项即封面图 */
    private List<ImageItem> images;
    private String githubUrl;
    private String demoUrl;
    private String videoUrl;
    private Boolean isFeatured;
    private Integer sortOrder;

    /** 项目状态（JSON 序列化为中文：已上线 / 筹备中 / 规划中；为空则 null） */
    private ProjectStatus status;

    /** 完成年份（4 位整数；为空则 null） */
    private Integer completionYear;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ImageItem implements Serializable {
        private String url;
        private String alt;
    }

    /** 便利：首张图作为封面，给列表视图使用 */
    public ImageItem cover() {
        if (images == null || images.isEmpty()) return null;
        return images.get(0);
    }
}
