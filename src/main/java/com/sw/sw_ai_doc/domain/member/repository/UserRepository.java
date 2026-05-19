package com.sw.sw_ai_doc.domain.member.repository;

import com.sw.sw_ai_doc.domain.member.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLoginId(String loginId);
    Optional<User> findByEmail(String email);
    boolean existsByLoginId(String loginId);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
