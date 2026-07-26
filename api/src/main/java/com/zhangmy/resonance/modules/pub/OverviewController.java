package com.zhangmy.resonance.modules.pub;

import com.zhangmy.resonance.common.R;
import com.zhangmy.resonance.modules.profile.ProfileService;
import com.zhangmy.resonance.modules.project.ProjectService;
import com.zhangmy.resonance.modules.project.dto.ProjectVO;
import com.zhangmy.resonance.modules.skill.SkillService;
import com.zhangmy.resonance.modules.skill.dto.SkillVO;
import com.zhangmy.resonance.modules.timeline.AwardService;
import com.zhangmy.resonance.modules.timeline.EducationService;
import com.zhangmy.resonance.modules.timeline.dto.AwardVO;
import com.zhangmy.resonance.modules.timeline.dto.EducationVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.List;

/**
 * 首屏聚合接口（减少前台首页 6 个请求到 1 个）
 * 数据完全来自后台管理端上传，无硬编码样例
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class OverviewController {

    private final ProfileService profileService;
    private final SkillService skillService;
    private final ProjectService projectService;
    private final AwardService awardService;
    private final EducationService educationService;

    @GetMapping("/overview")
    public R<OverviewVO> overview() {
        // 公开接口不再传 adminId，由各 Service 内部动态获取管理员 ID
        return R.ok(new OverviewVO(
                profileService.getPublic(null),
                skillService.listPublic(null),
                projectService.listPublic(null),
                awardService.listPublic(),
                educationService.listPublic()
        ));
    }

    @Data
    @AllArgsConstructor
    public static class OverviewVO implements Serializable {
        private com.zhangmy.resonance.modules.profile.dto.PublicProfileResponse profile;
        private java.util.List<SkillVO> skills;
        private java.util.List<ProjectVO> projects;
        private java.util.List<AwardVO> awards;
        private java.util.List<EducationVO> education;
    }
}
