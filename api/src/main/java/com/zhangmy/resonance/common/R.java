package com.zhangmy.resonance.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 全局统一响应包装
 */
@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class R<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int code;
    private String message;
    private T data;
    private long timestamp;

    public R() {
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> R<T> ok() {
        R<T> r = new R<>();
        r.setCode(ErrorCode.OK.getCode());
        r.setMessage(ErrorCode.OK.getMessage());
        return r;
    }

    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(ErrorCode.OK.getCode());
        r.setMessage(ErrorCode.OK.getMessage());
        r.setData(data);
        return r;
    }

    public static <T> R<T> fail(int code, String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    public static <T> R<T> fail(ErrorCode ec) {
        return fail(ec.getCode(), ec.getMessage());
    }

    public static <T> R<T> fail(ErrorCode ec, String extraMessage) {
        return fail(ec.getCode(), ec.getMessage() + ": " + extraMessage);
    }
}
