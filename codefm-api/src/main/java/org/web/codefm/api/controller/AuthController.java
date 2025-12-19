package org.web.codefm.api.controller;

import java.util.Objects;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.web.codefm.api.AuthApi;
import org.web.codefm.api.mapper.LoginResponseMapper;
import org.web.codefm.domain.session.LoginResponse;
import org.web.codefm.domain.usecase.AutenticationUseCase;
import org.web.codefm.model.LoginResponseDTO;

@Slf4j
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "basicAuth")
public class AuthController implements AuthApi {

    private final AutenticationUseCase autenticationUseCase;

  private final LoginResponseMapper loginResponseMapper;

    @Override
    public ResponseEntity<LoginResponseDTO> login(@RequestHeader("Authorization") String authorization) {

      HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder
          .getRequestAttributes())).getResponse();

      LoginResponse loginResponse = autenticationUseCase.login(authorization, response);
      LoginResponseDTO dto = loginResponseMapper.toDTO(loginResponse);
      return ResponseEntity.ok().body(dto);
    }

    @Override
    public ResponseEntity<String> refreshToken() {
      HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder
          .getRequestAttributes())).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getResponse();
        autenticationUseCase.refreshToken(request, response);
        return ResponseEntity.ok().body("Token refreshed successfully");

    }

    @Override
    public ResponseEntity<String> logout() {

      HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder
          .getRequestAttributes())).getResponse();

        autenticationUseCase.logout(response);

        return ResponseEntity.ok().body("Token removed successfully");
    }
}


