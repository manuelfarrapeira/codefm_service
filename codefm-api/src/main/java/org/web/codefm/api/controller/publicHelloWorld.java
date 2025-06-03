package org.web.codefm.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.web.codefm.api.PublicHelloWorldApi;
import org.web.codefm.api.utils.Logged;

@RestController
public class publicHelloWorld implements PublicHelloWorldApi {

    @Logged
    @Override
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Saludo genérico público");
    }
}
