package org.web.codefm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.web.codefm.domain.service.RestTemplateService;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RestTemplateServiceImpl implements RestTemplateService {

    private final RestTemplate restTemplate;

    @Override
    public <T, R> ResponseEntity<T> exchange(String url, HttpMethod method, R requestBody, HttpHeaders headers, Class<T> responseType, Map<String, ?> uriVariables) {

        if (headers == null) {
            headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
        }

        HttpEntity<R> entity = new HttpEntity<>(requestBody, headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        if (uriVariables != null) {
            for (Map.Entry<String, ?> entry : uriVariables.entrySet()) {
                builder.queryParam(entry.getKey(), entry.getValue());
            }
        }

        return restTemplate.exchange(
                builder.build().toUri(),
                method,
                entity,
                responseType
        );
    }

}
