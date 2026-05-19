package com.sw.sw_ai_doc.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login_id", length = 50, unique = true, nullable = false)
    private String loginId;

    @Column(length = 255, nullable = false)
    private String password;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 100, unique = true, nullable = false)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(length = 1)
    private String gender;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private UserStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = UserStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updatePhone(String phone) {
        this.phone = phone;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void withdraw(Long userId) {
        this.status = UserStatus.WITHDRAWN;
        this.loginId = "WITHDRAWN_" + userId + "_" + this.loginId;
        this.email = "WITHDRAWN_" + userId + "_" + this.email;
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public boolean isLocked() {
        return this.status == UserStatus.LOCKED;
    }
}
