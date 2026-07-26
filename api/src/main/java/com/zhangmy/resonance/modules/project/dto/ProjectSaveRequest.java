package com.zhangmy.resonance.modules.project.dto;

import com.zhangmy.resonance.modules.entity.ProjectStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProjectSaveRequest {

    @NotBlank(message = "项目标题不能为空")
    @Size(max = 120, message = "项目标题 ≤ 120")
    private String title;

    @NotBlank(message = "项目简介不能为空")
    @Size(max = 300, message = "项目简介 ≤ 300")
    private String summary;

    @NotBlank(message = "详细描述不能为空")
    @Size(max = 20000, message = "详细描述 ≤ 20000 字")
    private String description;

    @NotEmpty(message = "至少设置 1 个技术标签")
    @Size(max = 20, message = "标签数量 ≤ 20")
    private List<String> tags = new ArrayList<>();

    @Valid
    @Size(max = 20, message = "图片数量 ≤ 20 张")
    private List<ImageItem> images = new ArrayList<>();

    @Size(max = 512)
    private String githubUrl;

    @Size(max = 512)
    private String demoUrl;

    @Size(max = 512)
    private String videoUrl;

    private Boolean isFeatured = false;

    private Integer sortOrder = 0;

    /**
     * 项目状态：
     *   - 允许 null（未设置时前端显示占位符）
     *   - 入参支持中文 "已上线/筹备中/规划中" 或英文枚举名 ONLINE/PREPARING/PLANNING
     *   由 Service 层通过 ProjectStatus.fromLabelOrName() 统一解析
     */
    private String status;

    /** 完成年份：允许空；若填写则必须是 1900 ~ 2300 的 4 位整数（Service 层校验） */
    private Integer completionYear;

    /**
     * 项目概述正文（前台详情弹窗 italic 大字引用段独立内容）。
     * 允许空（前台 fallback 显示 description 兼容旧数据）。
     * 建议 ≤ 2000 字（上限 20000，与 description 一致）。
     */
    @Size(max = 20000, message = "项目概述正文 ≤ 20000 字")
    private String overview;

    @Data
    public static class ImageItem {
        @NotBlank(message = "图片 URL 不能为空")
        @Size(max = 1024, message = "图片 URL ≤ 1024")
        private String url;

        @Size(max = 255)
        private String alt;

        private Integer sortOrder = 0;
    }
}
