package com.zhangmy.resonance.modules.entity;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 项目状态枚举：
 *   ONLINE    -> 已上线
 *   PREPARING -> 筹备中
 *   PLANNING  -> 规划中
 *
 * Jackson 序列化时，使用中文作为 JSON 值，前后端直接以中文字符串交流，
 * 减少前端不必要的枚举映射翻译代码。
 * 数据库中仍存枚举名（ONLINE/PREPARING/PLANNING），由 JPA @Enumerated(STRING) 负责。
 */
public enum ProjectStatus {
    ONLINE("已上线"),
    PREPARING("筹备中"),
    PLANNING("规划中");

    private final String label;

    ProjectStatus(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public String dbName() {
        return name();
    }

    public static ProjectStatus fromLabelOrName(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isEmpty()) return null;
        for (ProjectStatus s : values()) {
            if (s.name().equalsIgnoreCase(v) || s.label.equals(v)) return s;
        }
        return null;
    }
}
