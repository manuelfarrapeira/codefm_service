package org.web.codefm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.web.codefm.domain.session.SessionUser;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionUserServiceImplTest {

    @Mock
    private SessionUser sessionUser;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SessionUserServiceImpl sessionUserService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void setSessionUserShouldPopulateSessionUserWhenValidJwtProvided() {
        Jwt jwt = Jwt.withTokenValue("token")
                .subject("123")
                .claim("preferred_username", "testUser")
                .claim("email", "test@test.com")
                .claim("realm_access", Map.of("roles", List.of("ROLE_USER")))
                .claim("resource_access", Map.of("codefm", Map.of("roles", List.of("USER"))))
                .header("alg", "none")
                .build();

        when(authentication.getPrincipal()).thenReturn(jwt);

        sessionUserService.setSessionUser();

        verify(sessionUser).setId("123");
        verify(sessionUser).setUsername("testUser");
        verify(sessionUser).setEmail("test@test.com");
        verify(sessionUser).setPermisos(List.of("ROLE_USER"));
        verify(sessionUser).setRoles(List.of("USER"));
    }

    @Test
    void setSessionUserShouldClearSessionUserWhenAuthenticationIsNull() {
        SecurityContextHolder.getContext().setAuthentication(null);

        sessionUserService.setSessionUser();

        verify(sessionUser).setId(null);
        verify(sessionUser).setUsername(null);
        verify(sessionUser).setEmail(null);
        verify(sessionUser).setRoles(any());
        verify(sessionUser).setPermisos(any());
    }

    @Test
    void setSessionUserShouldClearSessionUserWhenPrincipalIsNotJwt() {
        when(authentication.getPrincipal()).thenReturn("notAJwt");

        sessionUserService.setSessionUser();

        verify(sessionUser).setId(null);
        verify(sessionUser).setUsername(null);
        verify(sessionUser).setEmail(null);
        verify(sessionUser).setRoles(any());
        verify(sessionUser).setPermisos(any());
    }

    @Test
    void setSessionUserShouldHandleMissingRealmAccess() {
        Jwt jwt = Jwt.withTokenValue("token")
                .subject("123")
                .claim("preferred_username", "testUser")
                .claim("email", "test@test.com")
                .header("alg", "none")
                .build();

        when(authentication.getPrincipal()).thenReturn(jwt);

        sessionUserService.setSessionUser();

        verify(sessionUser, times(1)).setPermisos(any());


    }

    @Test
    void setSessionUserShouldHandleMissingResourceAccess() {
        Jwt jwt = Jwt.withTokenValue("token")
                .subject("123")
                .claim("preferred_username", "testUser")
                .claim("email", "test@test.com")
                .claim("realm_access", Map.of("roles", List.of("ROLE_USER")))
                .header("alg", "none")
                .build();

        when(authentication.getPrincipal()).thenReturn(jwt);

        sessionUserService.setSessionUser();

        verify(sessionUser, times(1)).setRoles(any());

    }
}