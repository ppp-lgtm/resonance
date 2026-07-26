package com.zhangmy.resonance.modules.skill.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SkillSaveRequest {
    @NotBlank(message = "技能名称不能为空")
    @Size(max = 50, message = "技能名称 ≤ 50")
    private String name;

    @NotBlank(message = "分类不能为空")
    @Size(max = 32, message = "分类 ≤ 32")
    private String category;

    @Size(max = 255, message = "图标 ≤ 255")
    private String icon;

    @NotNull(message = "熟练度不能为空")
    @Min(value = 0, message = "熟练度 ≥ 0")
    @Max(value = 100, message = "熟练度 ≤ 100")
    private Integer proficiency;

    private Integer sortOrder = 0;

    private Boolean visible = true;
}
