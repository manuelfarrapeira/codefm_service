package org.web.codefm.domain.session;

import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.io.Serializable;
import java.util.List;

@Data
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionUser implements Serializable {
    private String id;
    private String username;
    private String email;
    private List<String> roles;
    private List<String> permisos;

    @Override
    public String toString() {
        return "SessionUser{" +
                " username=" + username +
                ", roles=" + roles +
                ", permisos=" + permisos + " }";
    }

}

