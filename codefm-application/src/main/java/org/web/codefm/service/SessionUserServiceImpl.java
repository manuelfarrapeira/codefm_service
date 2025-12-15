package org.web.codefm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.enums.ResourceAccessClient;
import org.web.codefm.domain.service.SessionUserService;
import org.web.codefm.domain.session.SessionParameter;
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

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {

            sessionUser.setId(jwt.getSubject());
            sessionUser.setUsername(jwt.getClaimAsString("preferred_username"));
            sessionUser.setEmail(jwt.getClaimAsString("email"));

            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null) {
                sessionUser.setPermisos((List<String>) realmAccess.get("roles"));
            }

            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess != null) {
                List<String> allClientRoles = new ArrayList<>();
                for (ResourceAccessClient clientEnum : ResourceAccessClient.values()) {
                    String clientId = clientEnum.getClientId();
                    Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(clientId);
                    if (clientAccess != null && clientAccess.containsKey("roles")) {
                        Object rolesObject = clientAccess.get("roles");
                        if (rolesObject instanceof List) {
                            allClientRoles.addAll((List<String>) rolesObject);
                        } else {
                            log.warn("Roles for client '{}' are not a List<String>: {}", clientId, rolesObject);
                        }
                    }
                }
                sessionUser.setRoles(allClientRoles);
            }

            for (SessionParameter param : SessionParameter.values()) {
                if (jwt.hasClaim(param.getClaimName())) {
                    sessionUser.getParameters().put(param.getClaimName(), jwt.getClaim(param.getClaimName()).toString());
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
        sessionUser.getParameters().clear();
    }
}
