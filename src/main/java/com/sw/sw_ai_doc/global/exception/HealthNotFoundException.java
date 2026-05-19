package com.sw.sw_ai_doc.global.exception;

public class HealthNotFoundException extends RuntimeException {
    public HealthNotFoundException() {
        super("존재하지 않는 건강 기록이거나 접근 권한이 없습니다.");
    }
}
