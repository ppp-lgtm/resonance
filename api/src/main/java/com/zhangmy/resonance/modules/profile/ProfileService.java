package com.zhangmy.resonance.modules.profile;

import com.zhangmy.resonance.common.BizException;
import com.zhangmy.resonance.common.ErrorCode;
import com.zhangmy.resonance.config.security.CurrentAdmin;
import com.zhangmy.resonance.modules.entity.AdminUser;
import com.zhangmy.resonance.modules.entity.ContactInfo;
import com.zhangmy.resonance.modules.entity.Profile;
import com.zhangmy.resonance.modules.profile.dto.ProfileUpdateRequest;
import com.zhangmy.resonance.modules.profile.dto.PublicProfileResponse;
import com.zhangmy.resonance.modules.repo.AdminUserRepository;
import com.zhangmy.resonance.modules.repo.ContactInfoRepository;
import com.zhangmy.resonance.modules.repo.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ContactInfoRepository contactInfoRepository;
    private final AdminUserRepository adminUserRepository;

    /** 获取系统中唯一的管理员 ID（用于公开接口） */
    private Long getAdminId() {
        return adminUserRepository.findAll().stream()
                .findFirst()
                .map(AdminUser::getId)
                .orElse(null);
    }

    /* ============ 管理端：查询当前管理员的个人信息 + 全部联系方式 ============ */
    public ProfileUpdateRequest getAdminProfile() {
        Long adminId = CurrentAdmin.id();
        Profile p = profileRepository.findByAdminId(adminId).orElseGet(() -> {
            Profile n = new Profile();
            n.setAdminId(adminId);
            n.setName("");
            n.setTitles(new ArrayList<>());
            n.setSlogan("");
            n.setBio("");
            return profileRepository.save(n);
        });
        List<ContactInfo> contacts = contactInfoRepository.findByAdminIdOrderBySortOrderAscIdAsc(adminId);
        ProfileUpdateRequest dto = new ProfileUpdateRequest();
        dto.setName(p.getName());
        dto.setTitle(p.getTitles() == null ? new ArrayList<>() : new ArrayList<>(p.getTitles()));
        dto.setSlogan(p.getSlogan());
        dto.setBio(p.getBio());
        dto.setAvatar(p.getAvatarUrl());
        dto.setResumeUrl(p.getResumeUrl());
        dto.setLocation(p.getLocation());
        dto.setYearsExperience(p.getYearsExperience());
        dto.setFocusAreas(p.getFocusAreas() == null ? new ArrayList<>() : new ArrayList<>(p.getFocusAreas()));
        dto.setWorkingMode(p.getWorkingMode());
        dto.setContacts(contacts.stream().map(c -> {
            ProfileUpdateRequest.ContactItem item = new ProfileUpdateRequest.ContactItem();
            item.setId(c.getId());
            item.setPlatform(c.getPlatform());
            item.setIcon(c.getIcon());
            item.setValue(c.getValue());
            item.setLink(c.getLink());
            item.setCopyable(c.getCopyable());
            item.setVisible(c.getVisible());
            item.setSortOrder(c.getSortOrder());
            return item;
        }).collect(Collectors.toList()));
        return dto;
    }

    /* ============ 管理端：整体保存 ============ */
    @Transactional
    public ProfileUpdateRequest saveAdminProfile(ProfileUpdateRequest req) {
        Long adminId = CurrentAdmin.id();

        // 1. profile
        Profile profile = profileRepository.findByAdminId(adminId)
                .orElseGet(() -> {
                    Profile n = new Profile();
                    n.setAdminId(adminId);
                    return n;
                });
        profile.setName(req.getName());
        profile.setTitles(new ArrayList<>(req.getTitle()));
        profile.setSlogan(req.getSlogan());
        profile.setBio(req.getBio());
        profile.setAvatarUrl(req.getAvatar());
        profile.setResumeUrl(req.getResumeUrl());
        profile.setLocation(req.getLocation());
        profile.setYearsExperience(req.getYearsExperience());
        profile.setFocusAreas(req.getFocusAreas() == null ? new ArrayList<>() : new ArrayList<>(req.getFocusAreas()));
        profile.setWorkingMode(req.getWorkingMode());
        profileRepository.save(profile);

        // 2. contact: upsert by id; delete removed ids
        List<ContactInfo> existing = contactInfoRepository.findByAdminIdOrderBySortOrderAscIdAsc(adminId);
        Map<Long, ContactInfo> existMap = existing.stream()
                .collect(Collectors.toMap(ContactInfo::getId, x -> x, (a, b) -> a, LinkedHashMap::new));

        Set<Long> keepIds = new HashSet<>();
        int seq = 0;
        for (ProfileUpdateRequest.ContactItem it : req.getContacts()) {
            ContactInfo c;
            if (it.getId() != null && existMap.containsKey(it.getId())) {
                c = existMap.get(it.getId());
            } else {
                c = new ContactInfo();
                c.setAdminId(adminId);
            }
            c.setPlatform(it.getPlatform());
            c.setIcon(it.getIcon());
            c.setValue(it.getValue());
            c.setLink(it.getLink());
            c.setCopyable(it.getCopyable() != null ? it.getCopyable() : true);
            c.setVisible(it.getVisible() != null ? it.getVisible() : true);
            c.setSortOrder(it.getSortOrder() != null ? it.getSortOrder() : seq++);
            contactInfoRepository.save(c);
            if (c.getId() != null) keepIds.add(c.getId());
        }

        // 删除未保留的 id
        for (ContactInfo ex : existing) {
            if (!keepIds.contains(ex.getId())) contactInfoRepository.delete(ex);
        }

        return getAdminProfile();
    }

    /* ============ 公开：获取前台展示用（仅 visible = true 的联系方式）============ */
    public PublicProfileResponse getPublic(Long adminId) {
        // adminId 参数已废弃，统一使用系统中唯一的管理员 ID
        Long aid = (adminId != null) ? adminId : getAdminId();
        if (aid == null) {
            return new PublicProfileResponse();
        }
        return profileRepository.findByAdminId(aid)
                .map(p -> {
                    List<ContactInfo> list = contactInfoRepository.findByAdminIdAndVisibleTrueOrderBySortOrderAscIdAsc(aid);
                    List<PublicProfileResponse.ContactItemVO> cs = list.stream()
                            .map(c -> new PublicProfileResponse.ContactItemVO(
                                    c.getId(), c.getPlatform(), c.getIcon(), c.getValue(), c.getLink(), c.getCopyable()))
                            .collect(Collectors.toList());
                    return new PublicProfileResponse(
                            p.getName(),
                            p.getTitles() == null ? List.of() : p.getTitles(),
                            p.getSlogan(),
                            p.getBio(),
                            p.getAvatarUrl(),
                            p.getResumeUrl(),
                            p.getLocation(),
                            p.getYearsExperience(),
                            p.getFocusAreas() == null ? List.of() : p.getFocusAreas(),
                            p.getWorkingMode(),
                            cs);
                })
                .orElseGet(() -> new PublicProfileResponse());
    }
}
