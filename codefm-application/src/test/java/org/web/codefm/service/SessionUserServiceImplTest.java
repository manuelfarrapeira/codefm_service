package org.web.codefm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.web.codefm.domain.session.SessionUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionUserServiceImplTest {

    @Mock
    private SessionUser sessionUser;

    @Mock
    private Authentication authentication;

    private SessionUserServiceImpl sessionUserService;

    @BeforeEach
    void beforeEach() {
        when(this.sessionUser.getParameters()).thenReturn(new HashMap<>());
        SecurityContextHolder.getContext().setAuthentication(this.authentication);
        this.sessionUserService = new SessionUserServiceImpl(this.sessionUser);
    }

    @Nested
    class SetSessionUser {

        @Test
        void when_valid_jwt_is_provided_expect_session_user_to_be_populated() {
            final Jwt jwt = Jwt.withTokenValue("token")
                    .subject("123")
                    .claim("preferred_username", "testUser")
                    .claim("email", "test@test.com")
                    .claim("realm_access", Map.of("roles", List.of("ROLE_USER")))
                    .claim("resource_access", Map.of("codefm", Map.of("roles", List.of("USER"))))
                    .header("alg", "none")
                    .build();

            when(authentication.getPrincipal()).thenReturn(jwt);

            assertThatNoException().isThrownBy(() -> sessionUserService.setSessionUser());

            verify(sessionUser).setId("123");
            verify(sessionUser).setUsername("testUser");
            verify(sessionUser).setEmail("test@test.com");
            verify(sessionUser).setPermisos(List.of("ROLE_USER"));
            verify(sessionUser).setRoles(List.of("USER"));
        }

        @Test
        void when_authentication_is_null_expect_session_user_to_be_cleared() {
            SecurityContextHolder.getContext().setAuthentication(null);

            assertThatNoException().isThrownBy(() -> sessionUserService.setSessionUser());

            verify(sessionUser).setId(null);
            verify(sessionUser).setUsername(null);
            verify(sessionUser).setEmail(null);
            verify(sessionUser).setRoles(any());
            verify(sessionUser).setPermisos(any());
        }

        @Test
        void when_principal_is_not_jwt_expect_session_user_to_be_cleared() {
            when(authentication.getPrincipal()).thenReturn("notAJwt");

            assertThatNoException().isThrownBy(() -> sessionUserService.setSessionUser());

            verify(sessionUser).setId(null);
            verify(sessionUser).setUsername(null);
            verify(sessionUser).setEmail(null);
            verify(sessionUser).setRoles(any());
            verify(sessionUser).setPermisos(any());
        }

        @Test
        void when_realm_access_is_missing_expect_permissions_to_be_handled() {
            final Jwt jwt = Jwt.withTokenValue("token")
                    .subject("123")
                    .claim("preferred_username", "testUser")
                    .claim("email", "test@test.com")
                    .header("alg", "none")
                    .build();

            when(authentication.getPrincipal()).thenReturn(jwt);

            assertThatNoException().isThrownBy(() -> sessionUserService.setSessionUser());

            verify(sessionUser, times(1)).setPermisos(any());
        }

        @Test
        void when_resource_access_is_missing_expect_roles_to_be_handled() {
            final Jwt jwt = Jwt.withTokenValue("token")
                    .subject("123")
                    .claim("preferred_username", "testUser")
                    .claim("email", "test@test.com")
                    .claim("realm_access", Map.of("roles", List.of("ROLE_USER")))
                    .header("alg", "none")
                    .build();

            when(authentication.getPrincipal()).thenReturn(jwt);

            assertThatNoException().isThrownBy(() -> sessionUserService.setSessionUser());

            verify(sessionUser, times(1)).setRoles(any());
        }
    }
}