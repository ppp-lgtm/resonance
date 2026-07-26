package com.zhangmy.resonance.modules.auth;

import com.zhangmy.resonance.common.R;
import com.zhangmy.resonance.modules.auth.dto.JwtResponse;
import com.zhangmy.resonance.modules.auth.dto.LoginRequest;
import com.zhangmy.resonance.modules.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 获取系统管理员初始化状态（公开接口，无需鉴权）
     * 前端据此决定显示登录页还是注册页
     */
    @GetMapping("/status")
    public R<AuthService.SystemStatus> status() {
        return R.ok(authService.getSystemStatus());
    }

    /**
     * 登录 → 签发 JWT
     */
    @PostMapping("/login")
    public R<JwtResponse> login(@Valid @RequestBody LoginRequest req) {
        return R.ok(authService.login(req));
    }

    /**
     * 注册新管理账号 → 返回 JWT（注册成功后自动登录）
     * 仅当管理员表为空时允许注册
     */
    @PostMapping("/register")
    public R<JwtResponse> register(@Valid @RequestBody RegisterRequest req) {
        return R.ok(authService.register(req));
    }
}
