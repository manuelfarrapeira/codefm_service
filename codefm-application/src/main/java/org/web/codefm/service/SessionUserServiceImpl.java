package org.web.codefm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.service.SessionUserService;
import org.web.codefm.domain.session.SessionUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class SessionUserServiceImpl implements SessionUserService {

    private final SessionUser sessionUser;

    @Override
    public void setSessionUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        clearSessionUser();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();

            sessionUser.setId(jwt.getSubject());
            sessionUser.setUsername(jwt.getClaimAsString("preferred_username"));
            sessionUser.setEmail(jwt.getClaimAsString("email"));

            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null) {
                sessionUser.setPermisos((List<String>) realmAccess.get("roles"));
            }

            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess != null) {
                Map<String, Object> appAccess = (Map<String, Object>) resourceAccess.get("codefm");
                if (appAccess != null) {
                    sessionUser.setRoles((List<String>) appAccess.get("roles"));
                }
            }
        }
    }

    private void clearSessionUser() {
        sessionUser.setId(null);
        sessionUser.setUsername(null);
        sessionUser.setEmail(null);
        sessionUser.setRoles(new ArrayList<>());
        sessionUser.setPermisos(new ArrayList<>());
    }
}
