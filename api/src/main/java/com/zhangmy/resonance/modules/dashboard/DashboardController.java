package com.zhangmy.resonance.modules.dashboard;

import com.zhangmy.resonance.common.R;
import com.zhangmy.resonance.config.security.CurrentAdmin;
import com.zhangmy.resonance.modules.contact.service.ContactStats;
import com.zhangmy.resonance.modules.dashboard.dto.DashboardStats;
import com.zhangmy.resonance.modules.project.ProjectService;
import com.zhangmy.resonance.modules.repo.ContactInfoRepository;
import com.zhangmy.resonance.modules.skill.SkillService;
import com.zhangmy.resonance.modules.timeline.AwardService;
import com.zhangmy.resonance.modules.timeline.EducationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ProjectService projectService;
    private final SkillService skillService;
    private final AwardService awardService;
    private final EducationService educationService;
    private final ContactInfoRepository contactInfoRepository;

    @GetMapping("/api/admin/dashboard/stats")
    @ResponseBody
    public R<DashboardStats> stats() {
        Long adminId = CurrentAdmin.id();
        DashboardStats stats = new DashboardStats(
                projectService.countAll(adminId),
                skillService.countVisible(adminId),
                awardService.count(adminId),
                educationService.count(adminId),
                contactInfoRepository.findByAdminIdOrderBySortOrderAscIdAsc(adminId).size()
        );
        return R.ok(stats);
    }
}
