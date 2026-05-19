package com.sw.sw_ai_doc.domain.member.repository;

import com.sw.sw_ai_doc.domain.member.entity.User;
import com.sw.sw_ai_doc.domain.member.entity.UserStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    private User buildUser(String loginId, String email) {
        return User.builder()
                .loginId(loginId)
                .password("encodedPassword")
                .name("테스터")
                .email(email)
                .phone("01012345678")
                .birthDate(LocalDate.of(1995, 1, 1))
                .gender("M")
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    void findByLoginId_found() {
        em.persist(buildUser("user1", "user1@test.com"));
        em.flush();

        Optional<User> result = userRepository.findByLoginId("user1");

        assertThat(result).isPresent();
        assertThat(result.get().getLoginId()).isEqualTo("user1");
    }

    @Test
    void findByLoginId_notFound() {
        Optional<User> result = userRepository.findByLoginId("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    void existsByLoginId_true() {
        em.persist(buildUser("existingUser", "existing@test.com"));
        em.flush();

        assertThat(userRepository.existsByLoginId("existingUser")).isTrue();
    }

    @Test
    void existsByLoginId_false() {
        assertThat(userRepository.existsByLoginId("nobody")).isFalse();
    }

    @Test
    void existsByEmail_true() {
        em.persist(buildUser("user2", "unique@test.com"));
        em.flush();

        assertThat(userRepository.existsByEmail("unique@test.com")).isTrue();
    }

    @Test
    void existsByEmail_false() {
        assertThat(userRepository.existsByEmail("notexist@test.com")).isFalse();
    }

    @Test
    void findByEmail_found() {
        em.persist(buildUser("user3", "find@test.com"));
        em.flush();

        Optional<User> result = userRepository.findByEmail("find@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("find@test.com");
    }
}
