package com.zhangmy.resonance.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse implements Serializable {
    private String token;
    private String tokenType;
    private long expiresIn;
    private UserInfo user;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo implements Serializable {
        private Long id;
        private String username;
        private String avatarUrl;
        private Long createdAt;
    }
}
