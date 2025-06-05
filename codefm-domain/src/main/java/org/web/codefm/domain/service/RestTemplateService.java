package org.web.codefm.domain.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface RestTemplateService {

    /**
     * Método genérico para realizar llamadas HTTP.
     *
     * @param url          URL del servicio externo.
     * @param method       Método HTTP (GET, POST, etc.).
     * @param requestBody  Cuerpo del request (puede ser null).
     * @param headers      Headers personalizados (puede ser null).
     * @param responseType Tipo de la respuesta esperada.
     * @param uriVariables Variables para path o query params (puede ser null).
     * @param <T>          Tipo de la respuesta.
     * @param <R>          Tipo del request.
     * @return ResponseEntity con el tipo de respuesta especificado.
     */
    <T, R> ResponseEntity<T> exchange(String url, HttpMethod method, R requestBody, HttpHeaders headers, Class<T> responseType, Map<String, ?> uriVariables);

}
