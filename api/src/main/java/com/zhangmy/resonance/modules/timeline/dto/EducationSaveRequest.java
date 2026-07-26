package com.zhangmy.resonance.modules.timeline.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EducationSaveRequest {
    @NotBlank(message = "学校不能为空")
    @Size(max = 255)
    private String school;

    @NotBlank(message = "学位不能为空")
    @Size(max = 32)
    private String degree;

    @NotBlank(message = "专业不能为空")
    @Size(max = 255)
    private String major;

    @NotNull(message = "开始时间不能为空")
    private LocalDate startDate;

    /** 结束时间；为空代表至今 */
    private LocalDate endDate;

    @Size(max = 65000)
    private String description;

    private Integer sortOrder = 0;
}
