package com.zhangmy.resonance.modules.timeline.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EducationVO implements Serializable {
    private Long id;
    private String school;
    private String degree;
    private String major;
    /** YYYY-MM 或 YYYY-MM-DD */
    private String startDate;
    /** 空 = 至今 */
    private String endDate;
    private String description;
    private Integer sortOrder;
}
