package com.zhangmy.resonance.modules.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 前台展示（public）的聚合响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublicProfileResponse implements Serializable {
    private String name;
    private List<String> title;
    private String slogan;
    private String bio;
    private String avatar;
    private String resumeUrl;
    private String location;
    private Integer yearsExperience;
    private List<String> focusAreas;
    private String workingMode;
    private List<ContactItemVO> contacts;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ContactItemVO implements Serializable {
        private Long id;
        private String platform;
        private String icon;
        private String value;
        private String link;
        private Boolean copyable;
    }
}
