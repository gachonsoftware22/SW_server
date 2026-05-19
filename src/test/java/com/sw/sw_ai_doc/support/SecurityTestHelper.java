package com.sw.sw_ai_doc.support;

import com.sw.sw_ai_doc.global.security.UserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Collections;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

public class SecurityTestHelper {

    public static RequestPostProcessor mockUser(Long userId, String loginId) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                new UserPrincipal(userId, loginId), null, Collections.emptyList()
        );
        return authentication(auth);
    }

    public static RequestPostProcessor mockUser() {
        return mockUser(1L, "testUser");
    }
}
