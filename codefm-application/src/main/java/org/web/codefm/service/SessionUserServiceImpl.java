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

    private static final String ROLES_CLAIM = "roles";

    private final SessionUser sessionUser;

    @Override
    public void setSessionUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        clearSessionUser();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            setBasicUserInfo(jwt);
            setRealmPermissions(jwt);
            setResourceRoles(jwt);
            setSessionParameters(jwt);
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

    private void setBasicUserInfo(Jwt jwt) {
        sessionUser.setId(jwt.getSubject());
        sessionUser.setUsername(jwt.getClaimAsString("preferred_username"));
        sessionUser.setEmail(jwt.getClaimAsString("email"));
    }

    private void setRealmPermissions(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.get(ROLES_CLAIM) instanceof List) {
            sessionUser.setPermisos((List<String>) realmAccess.get(ROLES_CLAIM));
        }
    }

    private void setResourceRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess == null) {
            return;
        }

        List<String> allClientRoles = new ArrayList<>();
        for (ResourceAccessClient clientEnum : ResourceAccessClient.values()) {
            String clientId = clientEnum.getClientId();
            Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(clientId);

            if (clientAccess != null && clientAccess.containsKey(ROLES_CLAIM)) {
                Object rolesObject = clientAccess.get(ROLES_CLAIM);
                if (rolesObject instanceof List) {
                    allClientRoles.addAll((List<String>) rolesObject);
                } else {
                    log.warn("Roles for client '{}' are not a List<String>: {}", clientId, rolesObject);
                }
            }
        }
        sessionUser.setRoles(allClientRoles);
    }

    private void setSessionParameters(Jwt jwt) {
        for (SessionParameter param : SessionParameter.values()) {
            if (jwt.hasClaim(param.getClaimName())) {
                sessionUser.getParameters().put(param.getClaimName(), jwt.getClaim(param.getClaimName()).toString());
            }
        }
    }
}
