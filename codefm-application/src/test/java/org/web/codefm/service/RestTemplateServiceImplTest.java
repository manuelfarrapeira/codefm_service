package org.web.codefm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestTemplateServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    private RestTemplateServiceImpl restTemplateService;

    @BeforeEach
    void beforeEach() {
        this.restTemplateService = new RestTemplateServiceImpl(this.restTemplate);
    }

    @Nested
    class Exchange {

        @Test
        void when_request_body_headers_and_uri_variables_are_provided_expect_successful_exchange() {
            final String url = "http://api.example.com";
            final HttpMethod method = HttpMethod.POST;
            final String requestBody = "request";
            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            final Map<String, String> uriVariables = Map.of("key", "value");
            final ResponseEntity<String> expectedResponse = new ResponseEntity<>("response", HttpStatus.OK);

            when(restTemplate.exchange(any(URI.class), eq(method), any(HttpEntity.class), eq(String.class))).thenReturn(expectedResponse);

            final ResponseEntity<String> actualResponse = restTemplateService.exchange(url, method, requestBody, headers, String.class, uriVariables);

            assertThat(actualResponse).isEqualTo(expectedResponse);
        }

        @Test
        void when_headers_are_null_expect_successful_exchange() {
            final String url = "http://api.example.com";
            final HttpMethod method = HttpMethod.GET;
            final String requestBody = "request";
            final ResponseEntity<String> expectedResponse = new ResponseEntity<>("response", HttpStatus.OK);

            when(restTemplate.exchange(eq(URI.create(url)), eq(method), any(HttpEntity.class), eq(String.class))).thenReturn(expectedResponse);

            final ResponseEntity<String> actualResponse = restTemplateService.exchange(url, method, requestBody, null, String.class, null);

            assertThat(actualResponse).isEqualTo(expectedResponse);
        }

        @Test
        void when_uri_variables_are_null_expect_successful_exchange() {
            final String url = "http://api.example.com";
            final HttpMethod method = HttpMethod.PUT;
            final String requestBody = "request";
            final HttpHeaders headers = new HttpHeaders();
            final ResponseEntity<String> expectedResponse = new ResponseEntity<>("response", HttpStatus.OK);

            when(restTemplate.exchange(URI.create(url), method, new HttpEntity<>(requestBody, headers), String.class)).thenReturn(expectedResponse);

            final ResponseEntity<String> actualResponse = restTemplateService.exchange(url, method, requestBody, headers, String.class, null);

            assertThat(actualResponse).isEqualTo(expectedResponse);
        }
    }
}