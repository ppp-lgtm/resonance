package com.zhangmy.resonance.common;

import lombok.Getter;

/**
 * 业务异常（全局异常处理器翻译为错误码）
 */
@Getter
public class BizException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String detail;

    public BizException(ErrorCode ec) {
        super(ec.getMessage());
        this.errorCode = ec;
        this.detail = null;
    }

    public BizException(ErrorCode ec, String detail) {
        super(ec.getMessage() + ": " + detail);
        this.errorCode = ec;
        this.detail = detail;
    }

    public BizException(ErrorCode ec, String detail, Throwable cause) {
        super(ec.getMessage() + ": " + detail, cause);
        this.errorCode = ec;
        this.detail = detail;
    }
}
