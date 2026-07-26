package com.zhangmy.resonance.modules.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStats implements Serializable {
    private long projectCount;
    private long skillCount;
    private long awardCount;
    private long educationCount;
    private long contactCount;
}
