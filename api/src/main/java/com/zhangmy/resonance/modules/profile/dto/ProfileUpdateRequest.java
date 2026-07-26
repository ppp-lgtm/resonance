package com.zhangmy.resonance.modules.profile.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理端整体更新个人信息 + 联系方式列表。
 * id == null 的 contact 视为新增；未出现的 id 视为删除。
 */
@Data
public class ProfileUpdateRequest {
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度 ≤ 50")
    private String name;

    @NotEmpty(message = "头衔数组不能为空")
    @Size(max = 10, message = "头衔项数 ≤ 10")
    private List<@Size(max = 50, message = "单条头衔 ≤ 50") String> title = new ArrayList<>();

    @NotBlank(message = "Slogan 不能为空")
    @Size(max = 120, message = "Slogan 长度 ≤ 120")
    private String slogan;

    @NotBlank(message = "个人介绍不能为空")
    @Size(max = 5000, message = "个人介绍 ≤ 5000 字")
    private String bio;

    @Size(max = 512, message = "头像 URL ≤ 512")
    private String avatar;

    @Size(max = 512, message = "简历 URL ≤ 512")
    private String resumeUrl;

    @Size(max = 128, message = "所在城市 ≤ 128")
    private String location;

    private Integer yearsExperience;

    @Size(max = 20, message = "专注方向项数 ≤ 20")
    private List<@Size(max = 50, message = "单个专注方向 ≤ 50") String> focusAreas = new ArrayList<>();

    @Size(max = 128, message = "合作模式 ≤ 128")
    private String workingMode;

    @Valid
    @Size(max = 20, message = "联系方式 ≤ 20 条")
    private List<ContactItem> contacts = new ArrayList<>();

    @Data
    public static class ContactItem {
        /** null = 新增；有值 = 更新 */
        private Long id;

        @NotBlank(message = "平台名不能为空")
        @Size(max = 32)
        private String platform;

        @Size(max = 64)
        private String icon;

        @NotBlank(message = "联系方式值不能为空")
        @Size(max = 512)
        private String value;

        @Size(max = 1024)
        private String link;

        private Boolean copyable = true;

        private Boolean visible = true;

        private Integer sortOrder = 0;
    }
}
