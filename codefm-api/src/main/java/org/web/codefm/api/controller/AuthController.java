package org.web.codefm.api.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Generated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.web.codefm.api.AuthApi;
import org.web.codefm.domain.usecase.AutenticationUseCase;

@Slf4j
@RestController
@RequiredArgsConstructor
@Generated
@SecurityRequirement(name = "basicAuth")
public class AuthController implements AuthApi {

    private final AutenticationUseCase autenticationUseCase;

    @Override
    public ResponseEntity<String> login(@RequestHeader("Authorization") String authorization) {

        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getResponse();

        autenticationUseCase.login(authorization, response);
        return ResponseEntity.ok().body("Autenticated successful");
    }

    @Override
    public ResponseEntity<String> refreshToken() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getResponse();
        autenticationUseCase.refreshToken(request, response);
        return ResponseEntity.ok().body("Token refreshed successfully");

    }

    @Override
    public ResponseEntity<String> logout() {

        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getResponse();

        autenticationUseCase.logout(response);

        return ResponseEntity.ok().body("Token removed successfully");
    }
}


