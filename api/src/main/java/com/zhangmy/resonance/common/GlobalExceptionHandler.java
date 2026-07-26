package com.zhangmy.resonance.common;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /* ================= 业务异常 ================= */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<R<Void>> handleBiz(BizException e) {
        ErrorCode ec = e.getErrorCode();
        int httpStatus = switch (ec) {
            case UNAUTHORIZED, TOKEN_EXPIRED_OR_INVALID, BAD_CREDENTIALS -> 401;
            case FORBIDDEN -> 403;
            case NOT_FOUND -> 404;
            case DUPLICATE_SKILL_NAME, DUPLICATE_CONTACT -> 409;
            case UNSUPPORTED_FILE_TYPE, FILE_TOO_LARGE, PARAM_INVALID, PAYLOAD_TOO_LARGE -> 400;
            default -> 500;
        };
        String msg = e.getDetail() != null ? ec.getMessage() + ": " + e.getDetail() : ec.getMessage();
        log.warn("[BizException] code={} msg={}", ec.getCode(), msg);
        return ResponseEntity.status(httpStatus).body(R.fail(ec.getCode(), msg));
    }

    /* ================= Spring Validation ================= */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<R<Void>> handleValid(Exception e) {
        String msg;
        if (e instanceof MethodArgumentNotValidException mav) {
            msg = mav.getBindingResult().getFieldErrors().stream()
                    .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                    .collect(Collectors.joining("; "));
        } else {
            BindException be = (BindException) e;
            msg = be.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage).collect(Collectors.joining("; "));
        }
        return ResponseEntity.badRequest()
                .body(R.fail(ErrorCode.PARAM_INVALID.getCode(), ErrorCode.PARAM_INVALID.getMessage() + ": " + msg));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<R<Void>> handleConstraint(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage).collect(Collectors.joining("; "));
        return ResponseEntity.badRequest()
                .body(R.fail(ErrorCode.PARAM_INVALID.getCode(), ErrorCode.PARAM_INVALID.getMessage() + ": " + msg));
    }

    /* ================= Security ================= */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<R<Void>> handleAuth(Exception e) {
        log.warn("[Auth] {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(R.fail(ErrorCode.BAD_CREDENTIALS));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<R<Void>> handleDenied() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(R.fail(ErrorCode.FORBIDDEN));
    }

    /* ================= 404 ================= */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<R<Void>> handle404() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(R.fail(ErrorCode.NOT_FOUND));
    }

    /* ================= 文件上传超限 ================= */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<R<Void>> handleSize() {
        return ResponseEntity.badRequest().body(R.fail(ErrorCode.FILE_TOO_LARGE));
    }

    /* ================= 兜底 ================= */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<Void>> handleOther(Exception e) {
        log.error("[UnhandledException]", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(R.fail(ErrorCode.INTERNAL));
    }
}
