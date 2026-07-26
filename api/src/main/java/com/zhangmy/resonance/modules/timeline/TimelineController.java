package com.zhangmy.resonance.modules.timeline;

import com.zhangmy.resonance.common.R;
import com.zhangmy.resonance.config.security.CurrentAdmin;
import com.zhangmy.resonance.modules.timeline.dto.AwardSaveRequest;
import com.zhangmy.resonance.modules.timeline.dto.AwardVO;
import com.zhangmy.resonance.modules.timeline.dto.EducationSaveRequest;
import com.zhangmy.resonance.modules.timeline.dto.EducationVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TimelineController {

    private final AwardService awardService;
    private final EducationService educationService;

    /* ====== 公开（前台）====== */

    @GetMapping("/api/public/awards")
    public R<List<AwardVO>> listAwardsPublic() {
        return R.ok(awardService.listPublic());
    }

    @GetMapping("/api/public/education")
    public R<List<EducationVO>> listEducationPublic() {
        return R.ok(educationService.listPublic());
    }

    /* ====== 管理（后台）====== */

    @GetMapping("/api/admin/awards")
    public R<List<AwardVO>> listAwardsAdmin() {
        CurrentAdmin.id();
        return R.ok(awardService.listAdmin());
    }

    @PostMapping("/api/admin/awards")
    public R<AwardVO> createAward(@Valid @RequestBody AwardSaveRequest req) {
        CurrentAdmin.id();
        return R.ok(awardService.create(req));
    }

    @PutMapping("/api/admin/awards/{id}")
    public R<AwardVO> updateAward(@PathVariable Long id,
                                  @Valid @RequestBody AwardSaveRequest req) {
        CurrentAdmin.id();
        return R.ok(awardService.update(id, req));
    }

    @DeleteMapping("/api/admin/awards/{id}")
    public R<Void> deleteAward(@PathVariable Long id) {
        CurrentAdmin.id();
        awardService.delete(id);
        return R.ok();
    }

    /* === 教育经历 === */

    @GetMapping("/api/admin/education")
    public R<List<EducationVO>> listEduAdmin() {
        CurrentAdmin.id();
        return R.ok(educationService.listAdmin());
    }

    @PostMapping("/api/admin/education")
    public R<EducationVO> createEdu(@Valid @RequestBody EducationSaveRequest req) {
        CurrentAdmin.id();
        return R.ok(educationService.create(req));
    }

    @PutMapping("/api/admin/education/{id}")
    public R<EducationVO> updateEdu(@PathVariable Long id,
                                    @Valid @RequestBody EducationSaveRequest req) {
        CurrentAdmin.id();
        return R.ok(educationService.update(id, req));
    }

    @DeleteMapping("/api/admin/education/{id}")
    public R<Void> deleteEdu(@PathVariable Long id) {
        CurrentAdmin.id();
        educationService.delete(id);
        return R.ok();
    }
}
