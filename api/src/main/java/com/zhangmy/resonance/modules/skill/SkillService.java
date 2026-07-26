package com.zhangmy.resonance.modules.skill;

import com.zhangmy.resonance.common.BizException;
import com.zhangmy.resonance.common.ErrorCode;
import com.zhangmy.resonance.config.security.CurrentAdmin;
import com.zhangmy.resonance.modules.entity.Skill;
import com.zhangmy.resonance.modules.repo.AdminUserRepository;
import com.zhangmy.resonance.modules.repo.SkillRepository;
import com.zhangmy.resonance.modules.skill.dto.SkillSaveRequest;
import com.zhangmy.resonance.modules.skill.dto.SkillVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;
    private final AdminUserRepository adminUserRepository;

    /** 获取系统中唯一的管理员 ID（用于公开接口） */
    private Long getAdminId() {
        return adminUserRepository.findAll().stream()
                .findFirst()
                .map(com.zhangmy.resonance.modules.entity.AdminUser::getId)
                .orElse(null);
    }

    /* ========== 公开接口：只返回 visible=true ========== */
    public List<SkillVO> listPublic(String category) {
        Long aid = getAdminId();
        if (aid == null) return List.of();
        List<Skill> list = (category == null || category.isEmpty() || "全部".equals(category))
                ? skillRepository.findByAdminIdAndVisibleTrueOrderBySortOrderAscIdAsc(aid)
                : skillRepository.findByAdminIdAndCategoryAndVisibleTrueOrderBySortOrderAsc(aid, category);
        return toVO(list);
    }

    /* ========== 管理接口 ========== */
    public List<SkillVO> listAdmin(String category) {
        Long adminId = CurrentAdmin.id();
        List<Skill> list = (category == null || category.isEmpty() || "全部".equals(category))
                ? skillRepository.findByAdminIdOrderBySortOrderAscIdAsc(adminId)
                : skillRepository.findByAdminIdAndCategoryOrderBySortOrderAsc(adminId, category);
        return toVO(list);
    }

    public SkillVO create(SkillSaveRequest req) {
        Long adminId = CurrentAdmin.id();
        // 查重（同管理员下 skill name）
        if (skillRepository.findByAdminIdAndName(adminId, req.getName()).isPresent()) {
            throw new BizException(ErrorCode.DUPLICATE_SKILL_NAME);
        }
        Skill s = new Skill();
        s.setAdminId(adminId);
        s.setName(req.getName());
        s.setCategory(req.getCategory());
        s.setIcon(StringUtils.hasText(req.getIcon()) ? req.getIcon() : "🔧");
        s.setProficiency(req.getProficiency());
        s.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());
        s.setVisible(req.getVisible() == null ? true : req.getVisible());
        return toVO(skillRepository.save(s));
    }

    public SkillVO update(Long id, SkillSaveRequest req) {
        Long adminId = CurrentAdmin.id();
        Skill s = skillRepository.findById(id)
                .filter(x -> x.getAdminId().equals(adminId))
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "skill#" + id));
        // 重名检查（排除自己）
        skillRepository.findByAdminIdAndName(adminId, req.getName())
                .filter(x -> !x.getId().equals(id))
                .ifPresent(x -> { throw new BizException(ErrorCode.DUPLICATE_SKILL_NAME); });
        s.setName(req.getName());
        s.setCategory(req.getCategory());
        s.setIcon(StringUtils.hasText(req.getIcon()) ? req.getIcon() : s.getIcon());
        s.setProficiency(req.getProficiency());
        s.setSortOrder(req.getSortOrder() == null ? s.getSortOrder() : req.getSortOrder());
        s.setVisible(req.getVisible() == null ? s.getVisible() : req.getVisible());
        return toVO(skillRepository.save(s));
    }

    @Transactional
    public void delete(Long id) {
        Long adminId = CurrentAdmin.id();
        Skill s = skillRepository.findById(id)
                .filter(x -> x.getAdminId().equals(adminId))
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "skill#" + id));
        skillRepository.delete(s);
    }

    public long countVisible(Long adminId) {
        return skillRepository.findByAdminIdAndVisibleTrueOrderBySortOrderAscIdAsc(adminId).size();
    }

    /* ========== utils ========== */
    private List<SkillVO> toVO(List<Skill> list) {
        return list.stream()
                .map(s -> new SkillVO(s.getId(), s.getName(), s.getCategory(), s.getIcon(),
                        s.getProficiency(), s.getSortOrder(), s.getVisible()))
                .collect(Collectors.toList());
    }

    private SkillVO toVO(Skill s) {
        return new SkillVO(s.getId(), s.getName(), s.getCategory(), s.getIcon(),
                s.getProficiency(), s.getSortOrder(), s.getVisible());
    }
}
