package org.web.codefm.usecase;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.web.codefm.domain.exception.ErrorCodeEnum;
import org.web.codefm.domain.exception.UserNotFound;
import org.web.codefm.domain.session.TokenResponse;
import org.web.codefm.domain.usecase.AutenticationUseCase;

import java.util.Base64;

@Slf4j
@Service
public class AutenticationUseCaseImpl implements AutenticationUseCase {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String keycloakIssuerUri;

    @Value("${keycloak.client-id}")
    private String clientId;


    @Override
    public void login(String authHeader, HttpServletResponse response) {

        try {
            String base64Credentials = authHeader.substring("Basic".length()).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials));
            final String[] values = credentials.split(":", 2);
            String username = values[0];
            String password = values[1];

            String tokenEndpoint = keycloakIssuerUri + "/protocol/openid-connect/token";

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
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        String tokenEndpoint = keycloakIssuerUri + "/protocol/openid-connect/token";

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "refresh_token");
        map.add("client_id", clientId);
        map.add("refresh_token", refreshToken);

        getToken(response, map, tokenEndpoint);

    }

    @Override
    public void logout(HttpServletResponse response) {
        ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", "")
                .httpOnly(true).secure(true).path("/")
                .maxAge(0).sameSite("Strict").build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true).secure(true).path("/public/auth/refresh")
                .maxAge(0).sameSite("Strict").build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        String logoutEndpoint = keycloakIssuerUri + "/protocol/openid-connect/logout";
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForEntity(logoutEndpoint, null, Void.class);


    }

    private static void getToken(HttpServletResponse response, MultiValueMap<String, String> map, String tokenEndpoint) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<TokenResponse> tokenResponse = restTemplate.postForEntity(tokenEndpoint, request, TokenResponse.class);

        TokenResponse tokens = tokenResponse.getBody();

        ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", tokens.getAccessToken())
                .httpOnly(true).secure(true).path("/").maxAge(tokens.getExpiresIn()).sameSite("Strict").build();
        //secure(true) solo funcionará con https

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", tokens.getRefreshToken())
                .httpOnly(true).secure(true).path("/public/auth/refresh").maxAge(60 * 60 * 24).sameSite("Strict").build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    }
}
