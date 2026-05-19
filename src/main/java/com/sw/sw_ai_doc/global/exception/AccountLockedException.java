package com.sw.sw_ai_doc.global.exception;

public class AccountLockedException extends RuntimeException {
    public AccountLockedException() {
        super("잠금된 계정입니다.");
    }
}
