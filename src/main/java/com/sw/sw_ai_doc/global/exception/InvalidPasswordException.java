package com.sw.sw_ai_doc.global.exception;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException() {
        super("비밀번호가 일치하지 않습니다.");
    }
}
