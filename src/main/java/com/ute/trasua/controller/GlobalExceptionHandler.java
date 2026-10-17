package com.ute.trasua.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Bộ xử lý ngoại lệ tập trung cho toàn bộ REST API (Quy chuẩn AGENTS.md §2.4).
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Lỗi đối số không hợp lệ (400): {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "BAD_REQUEST",
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now()
        ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        log.warn("Lỗi trạng thái nghiệp vụ xung đột (409): {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "success", false,
                "status", HttpStatus.CONFLICT.value(),
                "error", "CONFLICT",
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now()
        ));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        log.warn("Xung đột khóa lạc quan (409): Hệ thống đang điều phối đồng thời, dữ liệu đã thay đổi!");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "success", false,
                "status", HttpStatus.CONFLICT.value(),
                "error", "OPTIMISTIC_LOCK_CONFLICT",
                "message", "Hệ thống đang điều phối đồng thời bởi người dùng khác. Vui lòng thử lại sau giây lát!",
                "timestamp", LocalDateTime.now()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        log.error("Lỗi không mong muốn trong REST API: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error", "INTERNAL_SERVER_ERROR",
                "message", "Đã xảy ra lỗi nội bộ trên máy chủ: " + ex.getMessage(),
                "timestamp", LocalDateTime.now()
        ));
    }
}
