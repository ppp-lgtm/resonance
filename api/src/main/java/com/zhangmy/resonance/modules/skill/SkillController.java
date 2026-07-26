package com.zhangmy.resonance.modules.skill;

import com.zhangmy.resonance.common.R;
import com.zhangmy.resonance.config.security.CurrentAdmin;
import com.zhangmy.resonance.modules.skill.dto.SkillSaveRequest;
import com.zhangmy.resonance.modules.skill.dto.SkillVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    /* ====== 公开（前台）====== */
    @GetMapping("/api/public/skills")
    public R<List<SkillVO>> listPublic(@RequestParam(value = "category", required = false) String category) {
        return R.ok(skillService.listPublic(category));
    }

    /* ====== 管理（后台）====== */
    @GetMapping("/api/admin/skills")
    public R<List<SkillVO>> listAdmin(@RequestParam(value = "category", required = false) String category) {
        CurrentAdmin.id();
        return R.ok(skillService.listAdmin(category));
    }

    @PostMapping("/api/admin/skills")
    public R<SkillVO> create(@Valid @RequestBody SkillSaveRequest req) {
        CurrentAdmin.id();
        return R.ok(skillService.create(req));
    }

    @PutMapping("/api/admin/skills/{id}")
    public R<SkillVO> update(@PathVariable("id") Long id,
                             @Valid @RequestBody SkillSaveRequest req) {
        CurrentAdmin.id();
        return R.ok(skillService.update(id, req));
    }

    @DeleteMapping("/api/admin/skills/{id}")
    public R<Void> delete(@PathVariable("id") Long id) {
        CurrentAdmin.id();
        skillService.delete(id);
        return R.ok();
    }
}
