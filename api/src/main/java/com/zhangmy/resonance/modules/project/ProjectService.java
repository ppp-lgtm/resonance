package com.zhangmy.resonance.modules.project;

import com.zhangmy.resonance.common.BizException;
import com.zhangmy.resonance.common.ErrorCode;
import com.zhangmy.resonance.config.security.CurrentAdmin;
import com.zhangmy.resonance.modules.entity.Project;
import com.zhangmy.resonance.modules.entity.ProjectImage;
import com.zhangmy.resonance.modules.entity.ProjectStatus;
import com.zhangmy.resonance.modules.project.dto.ProjectSaveRequest;
import com.zhangmy.resonance.modules.project.dto.ProjectVO;
import com.zhangmy.resonance.modules.repo.AdminUserRepository;
import com.zhangmy.resonance.modules.repo.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final AdminUserRepository adminUserRepository;

    /** 获取系统中唯一的管理员 ID（用于公开接口） */
    private Long getAdminId() {
        return adminUserRepository.findAll().stream()
                .findFirst()
                .map(com.zhangmy.resonance.modules.entity.AdminUser::getId)
                .orElse(null);
    }

    /* ================== 公开 ================== */

    /** 列表（仅已发布；按 sortOrder 升序 + created desc） */
    public List<ProjectVO> listPublic(String tag) {
        Long aid = getAdminId();
        if (aid == null) return List.of();
        List<Project> all = projectRepository.findByAdminIdAndPublishedTrueOrderBySortOrderAscIdDesc(aid);
        return toListVO(all, tag, true);
    }

    /** 详情（公开） */
    public ProjectVO detailPublic(Long id) {
        Project p = projectRepository.findDetailById(id)
                .filter(x -> Boolean.TRUE.equals(x.getPublished()))
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "project#" + id));
        return toDetailVO(p);
    }

    /* ================== 管理 ================== */

    public List<ProjectVO> listAdmin(String tag, String keyword) {
        Long adminId = CurrentAdmin.id();
        List<Project> all = projectRepository.listAdmin(adminId);
        return toListVO(all, tag, false);
    }

    public ProjectVO detailAdmin(Long id) {
        Long adminId = CurrentAdmin.id();
        Project p = projectRepository.findDetailById(id)
                .filter(x -> x.getAdminId().equals(adminId))
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "project#" + id));
        return toDetailVO(p);
    }

    @Transactional
    public ProjectVO create(ProjectSaveRequest req) {
        Long adminId = CurrentAdmin.id();
        Project p = new Project();
        p.setAdminId(adminId);
        applySave(p, req);
        p.setPublished(true);
        Project saved = projectRepository.save(p);
        return toDetailVO(saved);
    }

    @Transactional
    public ProjectVO update(Long id, ProjectSaveRequest req) {
        Long adminId = CurrentAdmin.id();
        Project p = projectRepository.findDetailById(id)
                .filter(x -> x.getAdminId().equals(adminId))
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "project#" + id));
        applySave(p, req);
        return toDetailVO(projectRepository.save(p));
    }

    @Transactional
    public void delete(Long id) {
        Long adminId = CurrentAdmin.id();
        Project p = projectRepository.findDetailById(id)
                .filter(x -> x.getAdminId().equals(adminId))
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "project#" + id));
        projectRepository.delete(p);
    }

    /* ================== utils ================== */

    private void applySave(Project p, ProjectSaveRequest req) {
        p.setTitle(req.getTitle());
        p.setSummary(req.getSummary());
        p.setDescription(req.getDescription());
        p.setTags(req.getTags() != null ? req.getTags() : new ArrayList<>());
        p.setGithubUrl(req.getGithubUrl());
        p.setDemoUrl(req.getDemoUrl());
        p.setVideoUrl(req.getVideoUrl());
        p.setFeatured(Boolean.TRUE.equals(req.getIsFeatured()));
        p.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());

        ProjectStatus parsedStatus = ProjectStatus.fromLabelOrName(req.getStatus());
        p.setStatus(parsedStatus);
        if (req.getCompletionYear() != null) {
            int y = req.getCompletionYear();
            if (y < 1900 || y > 2300) {
                throw new BizException(ErrorCode.PARAM_INVALID, "完成年份必须在 1900 ~ 2300 之间");
            }
            p.setCompletionYear(y);
        } else {
            p.setCompletionYear(null);
        }

        // overview：项目概述正文（前台 italic 大字引用段）。空串存 null，前台 fallback description
        String overview = StringUtils.hasText(req.getOverview()) ? req.getOverview().trim() : null;
        p.setOverview(overview);

        List<ProjectImage> images = new ArrayList<>();
        if (req.getImages() != null) {
            int idx = 0;
            for (ProjectSaveRequest.ImageItem im : req.getImages()) {
                ProjectImage img = new ProjectImage();
                img.setUrl(im.getUrl());
                img.setAltText(StringUtils.hasText(im.getAlt()) ? im.getAlt() : p.getTitle());
                img.setSortOrder(im.getSortOrder() != null ? im.getSortOrder() : idx++);
                images.add(img);
            }
        }
        p.replaceImages(images);
    }

    private List<ProjectVO> toListVO(List<Project> all, String tag, boolean skipNullTag) {
        return all.stream()
                .filter(p -> {
                    if (!StringUtils.hasText(tag)) return true;
                    if (skipNullTag && p.getTags() == null) return false;
                    return p.getTags() != null && p.getTags().contains(tag);
                })
                .map(this::toListVO)
                .collect(Collectors.toList());
    }

    private ProjectVO toListVO(Project p) {
        List<ProjectVO.ImageItem> imgs = p.getImages() == null ? new ArrayList<>() :
                p.getImages().stream()
                        .map(x -> new ProjectVO.ImageItem(x.getUrl(), x.getAltText()))
                        .limit(1)
                        .collect(Collectors.toList());
        return new ProjectVO(
                p.getId(), p.getTitle(), p.getSummary(), null,
                /* overview */ null, // 列表视图不返回 overview 正文，减少体积
                p.getTags() == null ? new ArrayList<>() : p.getTags(),
                imgs,
                p.getGithubUrl(), p.getDemoUrl(), p.getVideoUrl(),
                p.getFeatured(), p.getSortOrder(),
                p.getStatus(),
                p.getCompletionYear());
    }

    private ProjectVO toDetailVO(Project p) {
        List<ProjectVO.ImageItem> imgs = p.getImages() == null ? new ArrayList<>() :
                p.getImages().stream()
                        .map(x -> new ProjectVO.ImageItem(x.getUrl(), x.getAltText()))
                        .collect(Collectors.toList());
        return new ProjectVO(
                p.getId(), p.getTitle(), p.getSummary(), p.getDescription(),
                /* overview */ p.getOverview(), // 详情才返回完整项目概述
                p.getTags() == null ? new ArrayList<>() : p.getTags(),
                imgs,
                p.getGithubUrl(), p.getDemoUrl(), p.getVideoUrl(),
                p.getFeatured(), p.getSortOrder(),
                p.getStatus(),
                p.getCompletionYear());
    }

    public long countPublic(Long adminId) {
        return projectRepository.countByAdminIdAndPublishedTrue(adminId);
    }

    public long countAll(Long adminId) {
        return projectRepository.countByAdminId(adminId);
    }
}
