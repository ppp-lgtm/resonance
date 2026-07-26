package com.zhangmy.resonance.config.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录认证时注入到 SecurityContext 的 Principal
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminPrincipal implements Serializable {
    private Long id;
    private String username;
}
