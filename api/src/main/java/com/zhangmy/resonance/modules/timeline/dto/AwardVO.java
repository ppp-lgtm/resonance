package com.zhangmy.resonance.modules.timeline.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AwardVO implements Serializable {
    private Long id;
    private String title;
    private String issuer;
    /** ISO 字符串 YYYY-MM 或 YYYY-MM-DD */
    private String date;
    private String description;
    private String certificateUrl;
    private String coverUrl;
    private Integer sortOrder;
}
