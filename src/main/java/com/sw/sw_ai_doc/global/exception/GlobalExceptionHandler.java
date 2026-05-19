package com.sw.sw_ai_doc.global.exception;

import com.sw.sw_ai_doc.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, e.getMessage()));
    }

    @ExceptionHandler(DuplicateLoginIdException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateLoginId(DuplicateLoginIdException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(409, e.getMessage()));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateEmail(DuplicateEmailException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(409, e.getMessage()));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidPassword(InvalidPasswordException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, e.getMessage()));
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountLocked(AccountLockedException e) {
        return ResponseEntity.status(HttpStatus.LOCKED)
                .body(ApiResponse.error(423, e.getMessage()));
    }

    @ExceptionHandler(HealthNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleHealthNotFound(HealthNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, e.getMessage()));
    }

    @ExceptionHandler(PrescriptionNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handlePrescriptionNotFound(PrescriptionNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, e.getMessage()));
    }

    @ExceptionHandler(AiAnalysisException.class)
    public ResponseEntity<ApiResponse<Void>> handleAiAnalysis(AiAnalysisException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getDefaultMessage())
                .orElse("입력값이 올바르지 않습니다.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "서버 오류가 발생했습니다."));
    }
}
