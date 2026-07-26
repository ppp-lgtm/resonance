package com.zhangmy.resonance.modules.timeline;

import com.zhangmy.resonance.common.BizException;
import com.zhangmy.resonance.common.ErrorCode;
import com.zhangmy.resonance.config.security.CurrentAdmin;
import com.zhangmy.resonance.modules.entity.Award;
import com.zhangmy.resonance.modules.repo.AdminUserRepository;
import com.zhangmy.resonance.modules.repo.AwardRepository;
import com.zhangmy.resonance.modules.timeline.dto.AwardSaveRequest;
import com.zhangmy.resonance.modules.timeline.dto.AwardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AwardService {

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");
    private final AwardRepository awardRepository;
    private final AdminUserRepository adminUserRepository;

    /** 获取系统中唯一的管理员 ID（用于公开接口） */
    private Long getAdminId() {
        return adminUserRepository.findAll().stream()
                .findFirst()
                .map(com.zhangmy.resonance.modules.entity.AdminUser::getId)
                .orElse(null);
    }

    public List<AwardVO> listPublic() {
        Long aid = getAdminId();
        if (aid == null) return List.of();
        return toVO(awardRepository.findByAdminIdOrderByAwardDateDescSortOrderAscIdDesc(aid));
    }

    public List<AwardVO> listAdmin() {
        Long id = CurrentAdmin.id();
        return toVO(awardRepository.findByAdminIdOrderByAwardDateDescSortOrderAscIdDesc(id));
    }

    public AwardVO create(AwardSaveRequest req) {
        Long adminId = CurrentAdmin.id();
        Award a = new Award();
        a.setAdminId(adminId);
        a.setTitle(req.getTitle());
        a.setIssuer(req.getIssuer());
        a.setAwardDate(req.getDate());
        a.setDescription(req.getDescription());
        a.setCertificateUrl(req.getCertificateUrl());
        a.setCoverUrl(req.getCoverUrl());
        a.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());
        return toVO(awardRepository.save(a));
    }

    public AwardVO update(Long id, AwardSaveRequest req) {
        Long adminId = CurrentAdmin.id();
        Award a = awardRepository.findById(id)
                .filter(x -> x.getAdminId().equals(adminId))
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "award#" + id));
        a.setTitle(req.getTitle());
        a.setIssuer(req.getIssuer());
        a.setAwardDate(req.getDate());
        a.setDescription(req.getDescription());
        a.setCertificateUrl(req.getCertificateUrl());
        a.setCoverUrl(req.getCoverUrl());
        a.setSortOrder(req.getSortOrder() == null ? a.getSortOrder() : req.getSortOrder());
        return toVO(awardRepository.save(a));
    }

    @Transactional
    public void delete(Long id) {
        Long adminId = CurrentAdmin.id();
        Award a = awardRepository.findById(id)
                .filter(x -> x.getAdminId().equals(adminId))
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "award#" + id));
        awardRepository.delete(a);
    }

    public long count(Long adminId) {
        return awardRepository.findByAdminIdOrderByAwardDateDescSortOrderAscIdDesc(adminId).size();
    }

    private AwardVO toVO(Award a) {
        return new AwardVO(
                a.getId(), a.getTitle(), a.getIssuer(),
                a.getAwardDate() == null ? null : a.getAwardDate().format(MONTH_FMT),
                a.getDescription(),
                a.getCertificateUrl(),
                a.getCoverUrl(),
                a.getSortOrder()
        );
    }

    private List<AwardVO> toVO(List<Award> list) {
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }
}
