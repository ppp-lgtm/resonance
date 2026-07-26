package com.zhangmy.resonance.modules.project;

import com.zhangmy.resonance.common.R;
import com.zhangmy.resonance.config.security.CurrentAdmin;
import com.zhangmy.resonance.modules.project.dto.ProjectSaveRequest;
import com.zhangmy.resonance.modules.project.dto.ProjectVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /* ========== 公开 ========== */

    @GetMapping("/api/public/projects")
    public R<List<ProjectVO>> listPublic(@RequestParam(value = "tag", required = false) String tag) {
        return R.ok(projectService.listPublic(tag));
    }

    @GetMapping("/api/public/projects/{id}")
    public R<ProjectVO> detailPublic(@PathVariable("id") Long id) {
        return R.ok(projectService.detailPublic(id));
    }

    /* ========== 管理 ========== */

    @GetMapping("/api/admin/projects")
    public R<List<ProjectVO>> listAdmin(@RequestParam(value = "tag", required = false) String tag,
                                        @RequestParam(value = "keyword", required = false) String keyword) {
        CurrentAdmin.id();
        return R.ok(projectService.listAdmin(tag, keyword));
    }

    @GetMapping("/api/admin/projects/{id}")
    public R<ProjectVO> detailAdmin(@PathVariable("id") Long id) {
        CurrentAdmin.id();
        return R.ok(projectService.detailAdmin(id));
    }

    @PostMapping("/api/admin/projects")
    public R<ProjectVO> create(@Valid @RequestBody ProjectSaveRequest req) {
        CurrentAdmin.id();
        return R.ok(projectService.create(req));
    }

    @PutMapping("/api/admin/projects/{id}")
    public R<ProjectVO> update(@PathVariable("id") Long id,
                               @Valid @RequestBody ProjectSaveRequest req) {
        CurrentAdmin.id();
        return R.ok(projectService.update(id, req));
    }

    @DeleteMapping("/api/admin/projects/{id}")
    public R<Void> delete(@PathVariable("id") Long id) {
        CurrentAdmin.id();
        projectService.delete(id);
        return R.ok();
    }
}
