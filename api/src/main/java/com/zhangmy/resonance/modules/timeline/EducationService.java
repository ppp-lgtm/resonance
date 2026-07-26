package com.zhangmy.resonance.modules.timeline;

import com.zhangmy.resonance.common.BizException;
import com.zhangmy.resonance.common.ErrorCode;
import com.zhangmy.resonance.config.security.CurrentAdmin;
import com.zhangmy.resonance.modules.entity.Education;
import com.zhangmy.resonance.modules.repo.AdminUserRepository;
import com.zhangmy.resonance.modules.repo.EducationRepository;
import com.zhangmy.resonance.modules.timeline.dto.EducationSaveRequest;
import com.zhangmy.resonance.modules.timeline.dto.EducationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EducationService {

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");
    private final EducationRepository educationRepository;
    private final AdminUserRepository adminUserRepository;

    /** 获取系统中唯一的管理员 ID（用于公开接口） */
    private Long getAdminId() {
        return adminUserRepository.findAll().stream()
                .findFirst()
                .map(com.zhangmy.resonance.modules.entity.AdminUser::getId)
                .orElse(null);
    }

    public List<EducationVO> listPublic() {
        Long aid = getAdminId();
        if (aid == null) return List.of();
        return toVO(educationRepository.findByAdminIdOrderByStartDateDescSortOrderAscIdDesc(aid));
    }

    public List<EducationVO> listAdmin() {
        Long id = CurrentAdmin.id();
        return toVO(educationRepository.findByAdminIdOrderByStartDateDescSortOrderAscIdDesc(id));
    }

    public EducationVO create(EducationSaveRequest req) {
        Long adminId = CurrentAdmin.id();
        Education e = new Education();
        e.setAdminId(adminId);
        e.setSchool(req.getSchool());
        e.setDegree(req.getDegree());
        e.setMajor(req.getMajor());
        e.setStartDate(req.getStartDate());
        e.setEndDate(req.getEndDate());
        e.setDescription(req.getDescription());
        e.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());
        return toVO(educationRepository.save(e));
    }

    public EducationVO update(Long id, EducationSaveRequest req) {
        Long adminId = CurrentAdmin.id();
        Education e = educationRepository.findById(id)
                .filter(x -> x.getAdminId().equals(adminId))
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "education#" + id));
        e.setSchool(req.getSchool());
        e.setDegree(req.getDegree());
        e.setMajor(req.getMajor());
        e.setStartDate(req.getStartDate());
        e.setEndDate(req.getEndDate());
        e.setDescription(req.getDescription());
        e.setSortOrder(req.getSortOrder() == null ? e.getSortOrder() : req.getSortOrder());
        return toVO(educationRepository.save(e));
    }

    @Transactional
    public void delete(Long id) {
        Long adminId = CurrentAdmin.id();
        Education e = educationRepository.findById(id)
                .filter(x -> x.getAdminId().equals(adminId))
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "education#" + id));
        educationRepository.delete(e);
    }

    public long count(Long adminId) {
        return educationRepository.findByAdminIdOrderByStartDateDescSortOrderAscIdDesc(adminId).size();
    }

    private EducationVO toVO(Education e) {
        return new EducationVO(
                e.getId(), e.getSchool(), e.getDegree(), e.getMajor(),
                e.getStartDate() == null ? null : e.getStartDate().format(MONTH_FMT),
                e.getEndDate() == null ? null : e.getEndDate().format(MONTH_FMT),
                e.getDescription(),
                e.getSortOrder()
        );
    }

    private List<EducationVO> toVO(List<Education> list) {
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }
}
