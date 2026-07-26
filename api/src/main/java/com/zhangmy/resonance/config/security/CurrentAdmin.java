package com.zhangmy.resonance.config.security;

import com.zhangmy.resonance.common.BizException;
import com.zhangmy.resonance.common.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录管理员便捷获取
 */
public class CurrentAdmin {

    /** 获取当前登录管理员 ID；未登录抛 401 */
    public static Long id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof AdminPrincipal p) return p.getId();
        throw new BizException(ErrorCode.TOKEN_EXPIRED_OR_INVALID);
    }

    public static AdminPrincipal principal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof AdminPrincipal p) return p;
        throw new BizException(ErrorCode.TOKEN_EXPIRED_OR_INVALID);
    }

    public static Long idOrNull() {
        try { return id(); } catch (Exception e) { return null; }
    }
}
