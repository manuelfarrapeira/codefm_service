package org.web.codefm.api.controller;

import lombok.Generated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.web.codefm.api.PrivateHelloWorldApi;
import org.web.codefm.api.utils.Logged;
import org.web.codefm.domain.usecase.HelloWorldUseCase;

@RestController
@Slf4j
@Generated
@RequiredArgsConstructor
public class PrivateHelloWorld implements PrivateHelloWorldApi {

    private final HelloWorldUseCase helloWorldUseCase;


    @Logged
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    //@PreAuthorize("hasAuthority('WRITE_PERMISSION')")
    public ResponseEntity<String> hello(String usuario) {

        return ResponseEntity.ok(helloWorldUseCase.helloWorld(usuario));
    }

}
