package com.sw.sw_ai_doc.global.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
    public UserNotFoundException() {
        super("해당 사용자를 찾을 수 없습니다.");
    }
}
