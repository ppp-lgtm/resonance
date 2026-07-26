package com.zhangmy.resonance.modules.auth;

import com.zhangmy.resonance.common.BizException;
import com.zhangmy.resonance.common.ErrorCode;
import com.zhangmy.resonance.common.JwtUtils;
import com.zhangmy.resonance.modules.auth.dto.JwtResponse;
import com.zhangmy.resonance.modules.auth.dto.LoginRequest;
import com.zhangmy.resonance.modules.auth.dto.RegisterRequest;
import com.zhangmy.resonance.modules.entity.AdminUser;
import com.zhangmy.resonance.modules.repo.AdminUserRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    /**
     * 系统管理员初始化状态
     */
    @Data
    @Builder
    public static class SystemStatus {
        /** 是否已存在管理员账号 */
        private boolean hasAdmin;
        /** 管理员表中记录总数 */
        private long adminCount;
    }

    /**
     * 获取管理员初始化状态（公开接口，无需鉴权）
     */
    public SystemStatus getSystemStatus() {
        long count = adminUserRepository.count();
        return SystemStatus.builder()
                .hasAdmin(count > 0)
                .adminCount(count)
                .build();
    }

    public JwtResponse login(LoginRequest req) {
        AdminUser user = adminUserRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new BizException(ErrorCode.BAD_CREDENTIALS));
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BizException(ErrorCode.BAD_CREDENTIALS);
        }
        String token = jwtUtils.generate(user.getId(), user.getUsername());
        // 更新最近登录时间
        user.setLastLoginAt(LocalDateTime.now());
        adminUserRepository.save(user);

        Long createdAtTs = user.getCreatedAt() == null ? null :
                java.sql.Timestamp.valueOf(user.getCreatedAt()).getTime();
        return new JwtResponse(
                token,
                "Bearer",
                jwtUtils.getExpireSeconds(),
                new JwtResponse.UserInfo(user.getId(), user.getUsername(), user.getAvatarUrl(), createdAtTs)
        );
    }

    /**
     * 注册新管理账号（仅当管理员表为空时允许）
     * @param req 注册请求（用户名+密码）
     * @return 注册成功后返回 JWT
     */
    public JwtResponse register(RegisterRequest req) {
        // 单管理员机制：表非空则拒绝注册
        if (adminUserRepository.count() > 0) {
            throw new BizException(ErrorCode.FORBIDDEN, "管理员账号已存在，注册通道已关闭");
        }

        // 检查用户名是否已存在（理论上不会命中，但做防御）
        if (adminUserRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new BizException(ErrorCode.DUPLICATE_USERNAME, req.getUsername());
        }

        // 创建新用户
        AdminUser newUser = new AdminUser();
        newUser.setUsername(req.getUsername());
        newUser.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        newUser.setDisplayName(req.getUsername());
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        adminUserRepository.save(newUser);

        // 签发 JWT
        String token = jwtUtils.generate(newUser.getId(), newUser.getUsername());
        Long createdAtTs = newUser.getCreatedAt() == null ? null :
                java.sql.Timestamp.valueOf(newUser.getCreatedAt()).getTime();
        return new JwtResponse(
                token,
                "Bearer",
                jwtUtils.getExpireSeconds(),
                new JwtResponse.UserInfo(newUser.getId(), newUser.getUsername(), null, createdAtTs)
        );
    }
}
