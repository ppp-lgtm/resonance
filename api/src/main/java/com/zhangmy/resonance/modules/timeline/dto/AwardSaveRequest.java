package com.zhangmy.resonance.modules.timeline.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AwardSaveRequest {
    @NotBlank(message = "奖项名称不能为空")
    @Size(max = 255, message = "奖项名称 ≤ 255")
    private String title;

    @NotBlank(message = "颁发机构不能为空")
    @Size(max = 255)
    private String issuer;

    /**
     * 接受格式 YYYY-MM 或 YYYY-MM-DD；后端自动转为 LocalDate
     */
    @NotNull(message = "获得时间不能为空")
    private LocalDate date;

    @Size(max = 65000, message = "描述太长")
    private String description;

    @Size(max = 1024, message = "证书 URL ≤ 1024")
    private String certificateUrl;

    @Size(max = 1024, message = "封面图 URL ≤ 1024")
    private String coverUrl;

    private Integer sortOrder = 0;
}
