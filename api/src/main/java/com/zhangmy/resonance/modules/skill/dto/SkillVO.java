package com.zhangmy.resonance.modules.skill.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkillVO implements Serializable {
    private Long id;
    private String name;
    private String category;
    private String icon;
    private Integer proficiency;
    private Integer sortOrder;
    private Boolean visible;
}
