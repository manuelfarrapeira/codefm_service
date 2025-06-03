package org.web.codefm.domain.usecase;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AutenticationUseCase {

    void login(String authHeader, HttpServletResponse response);

    void refreshToken(HttpServletRequest httpServletRequest, HttpServletResponse response);

    void logout(HttpServletResponse response);

}
