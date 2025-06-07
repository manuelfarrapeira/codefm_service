package org.web.codefm.domain.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Service class that provides a wrapper for RestTemplate operations
 */
public interface RestTemplateService {

    /**
     * Generic method to perform HTTP calls.
     *
     * @param url          External service URL.
     * @param method       HTTP method (GET, POST, etc.).
     * @param requestBody  Request body (can be null).
     * @param headers      Custom headers (can be null).
     * @param responseType Expected response type.
     * @param uriVariables Variables for path or query params (can be null).
     * @param <T>          Response type.
     * @param <R>          Request type.
     * @return ResponseEntity with the specified response type.
     */
    <T, R> ResponseEntity<T> exchange(String url, HttpMethod method, R requestBody, HttpHeaders headers, Class<T> responseType, Map<String, ?> uriVariables);

}
