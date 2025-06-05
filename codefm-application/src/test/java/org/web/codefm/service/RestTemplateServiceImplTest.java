package org.web.codefm.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestTemplateServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RestTemplateServiceImpl restTemplateService;

    @Test
    void successfullyExchangeWithRequestBodyAndHeaders() {
        String url = "http://api.example.com";
        HttpMethod method = HttpMethod.POST;
        String requestBody = "request";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> uriVariables = Map.of("key", "value");
        ResponseEntity<String> expectedResponse = new ResponseEntity<>("response", HttpStatus.OK);

        when(restTemplate.exchange(any(URI.class), eq(method), any(HttpEntity.class), eq(String.class))).thenReturn(expectedResponse);

        ResponseEntity<String> actualResponse = restTemplateService.exchange(url, method, requestBody, headers, String.class, uriVariables);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void successfullyExchangeWithNullHeaders() {
        String url = "http://api.example.com";
        HttpMethod method = HttpMethod.GET;
        String requestBody = "request";
        ResponseEntity<String> expectedResponse = new ResponseEntity<>("response", HttpStatus.OK);

        when(restTemplate.exchange(eq(URI.create(url)), eq(method), any(HttpEntity.class), eq(String.class))).thenReturn(expectedResponse);

        ResponseEntity<String> actualResponse = restTemplateService.exchange(url, method, requestBody, null, String.class, null);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void successfullyExchangeWithNullUriVariables() {
        String url = "http://api.example.com";
        HttpMethod method = HttpMethod.PUT;
        String requestBody = "request";
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<String> expectedResponse = new ResponseEntity<>("response", HttpStatus.OK);

        when(restTemplate.exchange(URI.create(url), method, new HttpEntity<>(requestBody, headers), String.class)).thenReturn(expectedResponse);

        ResponseEntity<String> actualResponse = restTemplateService.exchange(url, method, requestBody, headers, String.class, null);

        assertEquals(expectedResponse, actualResponse);
    }
}