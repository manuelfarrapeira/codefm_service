package org.web.codefm.usecase;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.web.codefm.domain.exception.UserNotFound;
import org.web.codefm.domain.service.RestTemplateService;
import org.web.codefm.domain.session.TokenResponse;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutenticationUseCaseImplTest {

    @Mock
    private RestTemplateService restTemplateService;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AutenticationUseCaseImpl autenticationUseCase;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(autenticationUseCase, "keycloakIssuerUri", "http://localhost:8080/auth/realms/test");
        ReflectionTestUtils.setField(autenticationUseCase, "tokenPath", "/protocol/openid-connect/token");
        ReflectionTestUtils.setField(autenticationUseCase, "logoutPath", "/protocol/openid-connect/logout");
        ReflectionTestUtils.setField(autenticationUseCase, "clientId", "test-client");
    }

    @Test
    void loginSuccessfully() {
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken("access-token");
        tokenResponse.setRefreshToken("refresh-token");
        tokenResponse.setExpiresIn(300);

        ResponseEntity<TokenResponse> response = new ResponseEntity<>(tokenResponse, HttpStatus.OK);
        when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(MultiValueMap.class), any(HttpHeaders.class), eq(TokenResponse.class), isNull()))
                .thenReturn(response);

        assertDoesNotThrow(() -> autenticationUseCase.login("Basic dXNlcjpwYXNz", this.response));
    }

    @Test
    void loginFailsWithInvalidCredentials() {
        when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(MultiValueMap.class), any(HttpHeaders.class), eq(TokenResponse.class), isNull()))
                .thenThrow(new org.springframework.web.client.HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        assertThrows(UserNotFound.class, () -> autenticationUseCase.login("Basic dXNlcjpwYXNz", response));
    }

    @Test
    void refreshTokenSuccessfully() {
        Cookie refreshTokenCookie = new Cookie("refresh_token", "refresh-token-value");
        Cookie[] cookies = {refreshTokenCookie};
        when(request.getCookies()).thenReturn(cookies);

        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken("new-access-token");
        tokenResponse.setRefreshToken("new-refresh-token");
        tokenResponse.setExpiresIn(300);

        ResponseEntity<TokenResponse> response = new ResponseEntity<>(tokenResponse, HttpStatus.OK);
        when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(MultiValueMap.class), any(HttpHeaders.class), eq(TokenResponse.class), isNull()))
                .thenReturn(response);

        assertDoesNotThrow(() -> autenticationUseCase.refreshToken(request, this.response));
    }

    @Test
    void refreshTokenWithNoCookies() {
        when(request.getCookies()).thenReturn(null);
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken("new-access-token");
        tokenResponse.setRefreshToken("new-refresh-token");
        tokenResponse.setExpiresIn(300);

        ResponseEntity<TokenResponse> mockResponse = new ResponseEntity<>(tokenResponse, HttpStatus.OK);
        when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(MultiValueMap.class), any(HttpHeaders.class), eq(TokenResponse.class), isNull()))
                .thenReturn(mockResponse);

        assertDoesNotThrow(() -> autenticationUseCase.refreshToken(request, response));
    }

    @Test
    void logoutSuccessfully() {
        ResponseEntity<Void> mockResponse = new ResponseEntity<>(HttpStatus.OK);
        when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), isNull(), isNull(), eq(Void.class), isNull()))
                .thenReturn(mockResponse);

        assertDoesNotThrow(() -> autenticationUseCase.logout(response));
        verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
    }
}