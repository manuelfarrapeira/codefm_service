package org.web.codefm.usecase;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.UserNotFound;
import org.web.codefm.domain.service.RestTemplateService;
import org.web.codefm.domain.session.TokenResponse;
import org.web.codefm.domain.usecase.AutenticationUseCase;

import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutenticationUseCaseImpl implements AutenticationUseCase {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String keycloakIssuerUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.token-path}")
    private String tokenPath;

    @Value("${spring.security.oauth2.resourceserver.jwt.logout-path}")
    private String logoutPath;

    @Value("${keycloak.client-id}")
    private String clientId;

    private static final String REFRESH_TOKEN = "refresh_token";

    private static final String ACCESS_TOKEN = "access_token";

    private static final String SAMESITE = "Strict";

    private static final String REFRESH_PATH = "public/auth/refresh";

    private final RestTemplateService restTemplateService;


    @Override
    public void login(String authHeader, HttpServletResponse response) {

        try {
            String base64Credentials = authHeader.substring("Basic".length()).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials));
            final String[] values = credentials.split(":", 2);
            String username = values[0];
            String password = values[1];

            String tokenEndpoint = keycloakIssuerUri + tokenPath;

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", "password");
            map.add("client_id", clientId);
            map.add("username", username);
            map.add("password", password);

            getToken(response, map, tokenEndpoint);

        } catch (HttpClientErrorException e) {
            log.error("Authentication error: {}", e.getMessage());
            throw new UserNotFound(ErrorCodeEnum.RESOURCE_NOT_FOUND, "User not found");
        }

    }

    @Override
    public void refreshToken(HttpServletRequest httpServletRequest, HttpServletResponse response) {

        Cookie[] cookies = httpServletRequest.getCookies();
        String refreshToken = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REFRESH_TOKEN.equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        String tokenEndpoint = keycloakIssuerUri + tokenPath;

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", REFRESH_TOKEN);
        map.add("client_id", clientId);
        map.add(REFRESH_TOKEN, refreshToken);

        getToken(response, map, tokenEndpoint);

    }

    @Override
    public void logout(HttpServletResponse response) {
        ResponseCookie accessTokenCookie = ResponseCookie.from(ACCESS_TOKEN, "")
                .httpOnly(true).secure(true).path("/")
                .maxAge(0).sameSite(SAMESITE).build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN, "")
                .httpOnly(true).secure(true).path(REFRESH_PATH)
                .maxAge(0).sameSite(SAMESITE).build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        String logoutEndpoint = keycloakIssuerUri + logoutPath;

        restTemplateService.exchange(logoutEndpoint, HttpMethod.POST, null, null, Void.class, null);

    }

    private void getToken(HttpServletResponse response, MultiValueMap<String, String> map, String tokenEndpoint) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<TokenResponse> tokenResponse = restTemplateService.exchange(tokenEndpoint, HttpMethod.POST, map, headers, TokenResponse.class, null);

        TokenResponse tokens = tokenResponse.getBody();

        ResponseCookie accessTokenCookie = ResponseCookie.from(ACCESS_TOKEN, tokens.getAccessToken())
                .httpOnly(true).secure(true).path("/").maxAge(tokens.getExpiresIn()).sameSite(SAMESITE).build();
        //secure(true) solo funcionará con https

        ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN, tokens.getRefreshToken())
                .httpOnly(true).secure(true).path(REFRESH_PATH).maxAge(43200).sameSite(SAMESITE).build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    }
}
