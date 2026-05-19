package com.sw.sw_ai_doc.global.exception;

public class PrescriptionNotFoundException extends RuntimeException {
    public PrescriptionNotFoundException() {
        super("존재하지 않는 처방전이거나 접근 권한이 없습니다.");
    }
}
