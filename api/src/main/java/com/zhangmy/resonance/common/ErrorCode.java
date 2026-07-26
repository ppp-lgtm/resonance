package com.zhangmy.resonance.common;

import lombok.Getter;

/**
 * 全局错误码枚举（对应接口文档）
 */
@Getter
public enum ErrorCode {
    OK(200, "success"),

    PARAM_INVALID(10001, "参数校验失败"),
    UNSUPPORTED_FILE_TYPE(10002, "不支持的文件类型"),
    FILE_TOO_LARGE(10003, "文件过大（≤ 10MB）"),

    UNAUTHORIZED(11001, "未登录或 Token 缺失"),
    TOKEN_EXPIRED_OR_INVALID(11002, "Token 已过期或无效"),
    BAD_CREDENTIALS(11003, "用户名或密码错误"),

    FORBIDDEN(12001, "无权限访问此资源"),

    NOT_FOUND(13001, "资源不存在"),

    DUPLICATE_SKILL_NAME(14001, "技能名称已存在"),
    DUPLICATE_CONTACT(14002, "联系方式已存在"),
    DUPLICATE_USERNAME(14003, "用户名已被注册"),

    PAYLOAD_TOO_LARGE(15001, "请求体过大"),

    INTERNAL(50000, "服务器内部错误"),
    STORAGE_UNAVAILABLE(50301, "对象存储服务不可用");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
