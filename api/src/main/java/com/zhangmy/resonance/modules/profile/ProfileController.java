package com.zhangmy.resonance.modules.profile;

import com.zhangmy.resonance.common.R;
import com.zhangmy.resonance.config.security.CurrentAdmin;
import com.zhangmy.resonance.modules.profile.dto.ProfileUpdateRequest;
import com.zhangmy.resonance.modules.profile.dto.PublicProfileResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /* ============ 公开接口（前台 HR-Web 用）============ */
    @GetMapping("/api/public/profile")
    public R<PublicProfileResponse> getPublic() {
        // adminId=null 让 Service 内部动态获取管理员 ID
        return R.ok(profileService.getPublic(null));
    }

    /* ============ 管理接口 ============ */

    /** 回显个人信息 + 联系方式数组 */
    @GetMapping("/api/admin/profile")
    public R<ProfileUpdateRequest> getAdmin() {
        CurrentAdmin.id();  // 仅做鉴权
        return R.ok(profileService.getAdminProfile());
    }

    /** 整体保存（含新增/更新/删除联系方式） */
    @PutMapping("/api/admin/profile")
    public R<ProfileUpdateRequest> saveAdmin(@Valid @RequestBody ProfileUpdateRequest req) {
        CurrentAdmin.id();  // 仅做鉴权
        return R.ok(profileService.saveAdminProfile(req));
    }
}
