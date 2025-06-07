package org.web.codefm.domain.usecase;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interface that defines authentication operations for the application.
 * Handles the authentication lifecycle: login, refresh token and logout.
 */
public interface AutenticationUseCase {

    /**
     * Performs the user login process.
     *
     * @param authHeader Authorization header containing credentials in Basic Auth format
     * @param response   HttpServletResponse object to set authentication cookies
     */
    void login(String authHeader, HttpServletResponse response);

    /**
     * Refreshes the access token using the refresh token.
     *
     * @param httpServletRequest HttpServletRequest object containing the refresh token cookie
     * @param response HttpServletResponse object to set new cookies
     */
    void refreshToken(HttpServletRequest httpServletRequest, HttpServletResponse response);

    /**
     * Logs out the user and clears authentication cookies.
     *
     * @param response HttpServletResponse object to clear authentication cookies
     */
    void logout(HttpServletResponse response);
}
