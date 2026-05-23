package org.web.codefm.usecase;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.web.codefm.domain.exception.UserNotFound;
import org.web.codefm.domain.service.RestTemplateService;
import org.web.codefm.domain.session.LoginResponse;
import org.web.codefm.domain.session.TokenResponse;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutenticationUseCaseImplTest {

    private AutenticationUseCaseImpl useCase;

    @Mock
    RestTemplateService restTemplateService;

    @Mock
    HttpServletResponse response;

    @Mock
    HttpServletRequest request;

    private static final String KEYCLOAK_ISSUER_URI = "http://localhost:8080/realms/test";
    private static final String TOKEN_PATH = "/protocol/openid-connect/token";
    private static final String LOGOUT_PATH = "/protocol/openid-connect/logout";
    private static final String CLIENT_ID = "test-client";

    @BeforeEach
    void beforeEach() {
        useCase = new AutenticationUseCaseImpl(restTemplateService);
        ReflectionTestUtils.setField(useCase, "keycloakIssuerUri", KEYCLOAK_ISSUER_URI);
        ReflectionTestUtils.setField(useCase, "tokenPath", TOKEN_PATH);
        ReflectionTestUtils.setField(useCase, "logoutPath", LOGOUT_PATH);
        ReflectionTestUtils.setField(useCase, "clientId", CLIENT_ID);
    }

    @Nested
    class Login {

        @Test
        void when_valid_credentials_expect_login_response_returned() {
            final String authHeader = "Basic " + Base64.getEncoder().encodeToString("testuser:testpass".getBytes());
            final String accessToken = createJwtToken("John", "Doe");
            final TokenResponse tokenResponse = new TokenResponse(accessToken, "refresh-token-123", 3600, "Bearer");
            when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull()))
                    .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));

            final LoginResponse result = useCase.login(authHeader, response);

            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo(accessToken);
            assertThat(result.getUserName()).isEqualTo("John Doe");
            verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
        }

        @Test
        void when_valid_credentials_expect_access_token_cookie_set_with_correct_attributes() {
            final String authHeader = "Basic " + Base64.getEncoder().encodeToString("user:pass".getBytes());
            final String accessToken = createJwtToken("Jane", "Smith");
            final TokenResponse tokenResponse = new TokenResponse(accessToken, "refresh-token", 3600, "Bearer");
            when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull()))
                    .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));
            final ArgumentCaptor<String> cookieCaptor = ArgumentCaptor.forClass(String.class);

            useCase.login(authHeader, response);

            verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), cookieCaptor.capture());
            final String accessTokenCookie = cookieCaptor.getAllValues().get(0);
            assertThat(accessTokenCookie).contains("access_token=" + accessToken);
            assertThat(accessTokenCookie).contains("HttpOnly");
            assertThat(accessTokenCookie).contains("Secure");
            assertThat(accessTokenCookie).contains("SameSite=Strict");
            assertThat(accessTokenCookie).contains("Max-Age=43200");
            assertThat(accessTokenCookie).contains("Path=/");
        }

        @Test
        void when_valid_credentials_expect_refresh_token_cookie_set_with_correct_attributes() {
            final String authHeader = "Basic " + Base64.getEncoder().encodeToString("user:pass".getBytes());
            final String accessToken = createJwtToken("Jane", "Smith");
            final String refreshToken = "refresh-token-xyz";
            final TokenResponse tokenResponse = new TokenResponse(accessToken, refreshToken, 3600, "Bearer");
            when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull()))
                    .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));
            final ArgumentCaptor<String> cookieCaptor = ArgumentCaptor.forClass(String.class);

            useCase.login(authHeader, response);

            verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), cookieCaptor.capture());
            final String refreshTokenCookie = cookieCaptor.getAllValues().get(1);
            assertThat(refreshTokenCookie).contains("refresh_token=" + refreshToken);
            assertThat(refreshTokenCookie).contains("HttpOnly");
            assertThat(refreshTokenCookie).contains("Secure");
            assertThat(refreshTokenCookie).contains("SameSite=Strict");
            assertThat(refreshTokenCookie).contains("Max-Age=43200");
            assertThat(refreshTokenCookie).contains("Path=public/auth/refresh");
        }

        @Test
        void when_invalid_credentials_expect_user_not_found_thrown() {
            final String authHeader = "Basic " + Base64.getEncoder().encodeToString("wronguser:wrongpass".getBytes());
            when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull()))
                    .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

            final ThrowingCallable result = () -> useCase.login(authHeader, response);

            assertThatThrownBy(result).isInstanceOf(UserNotFound.class);
        }

        @Test
        void when_token_has_both_given_and_family_name_expect_full_name_returned() {
            final String authHeader = "Basic " + Base64.getEncoder().encodeToString("user:pass".getBytes());
            final String accessToken = createJwtToken("Alice", "Wonder");
            when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull()))
                    .thenReturn(new ResponseEntity<>(new TokenResponse(accessToken, "refresh", 3600, "Bearer"), HttpStatus.OK));

            final LoginResponse result = useCase.login(authHeader, response);

            assertThat(result.getUserName()).isEqualTo("Alice Wonder");
        }

        @Test
        void when_token_has_only_given_name_expect_partial_name_returned() {
            final String authHeader = "Basic " + Base64.getEncoder().encodeToString("user:pass".getBytes());
            final String accessToken = createJwtTokenWithOnlyGivenName("SingleName");
            when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull()))
                    .thenReturn(new ResponseEntity<>(new TokenResponse(accessToken, "refresh", 3600, "Bearer"), HttpStatus.OK));

            final LoginResponse result = useCase.login(authHeader, response);

            assertThat(result.getUserName()).isEqualTo("SingleName ");
        }

        @Test
        void when_token_has_no_names_expect_empty_name_returned() {
            final String authHeader = "Basic " + Base64.getEncoder().encodeToString("user:pass".getBytes());
            final String accessToken = createJwtTokenWithoutNames();
            when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull()))
                    .thenReturn(new ResponseEntity<>(new TokenResponse(accessToken, "refresh", 3600, "Bearer"), HttpStatus.OK));

            final LoginResponse result = useCase.login(authHeader, response);

            assertThat(result.getUserName()).isEqualTo(" ");
        }

        @Test
        void when_base64_credentials_with_special_chars_expect_decoded_correctly() {
            final String authHeader = "Basic " + Base64.getEncoder().encodeToString("user@example.com:p@ssw0rd!".getBytes());
            final String accessToken = createJwtToken("Test", "User");
            when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull()))
                    .thenReturn(new ResponseEntity<>(new TokenResponse(accessToken, "refresh", 3600, "Bearer"), HttpStatus.OK));

            final LoginResponse result = useCase.login(authHeader, response);

            assertThat(result).isNotNull();
            assertThat(result.getUserName()).isEqualTo("Test User");
        }

        @Test
        void when_password_contains_colon_expect_login_successful() {
            final String authHeader = "Basic " + Base64.getEncoder().encodeToString("testuser:pass:word:123".getBytes());
            final String accessToken = createJwtToken("Test", "User");
            when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull()))
                    .thenReturn(new ResponseEntity<>(new TokenResponse(accessToken, "refresh", 3600, "Bearer"), HttpStatus.OK));

            final LoginResponse result = useCase.login(authHeader, response);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    class RefreshToken {

        @Test
        void when_valid_refresh_token_cookie_expect_new_tokens_set() {
            final Cookie refreshTokenCookie = new Cookie("refresh_token", "valid-refresh-token");
            when(request.getCookies()).thenReturn(new Cookie[]{refreshTokenCookie});
            when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull()))
                    .thenReturn(new ResponseEntity<>(new TokenResponse("new-access-token", "new-refresh-token", 3600, "Bearer"), HttpStatus.OK));

            useCase.refreshToken(request, response);

            verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
        }

        @Test
        void when_no_cookies_expect_token_endpoint_still_called() {
            when(request.getCookies()).thenReturn(null);
            when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull()))
                    .thenReturn(new ResponseEntity<>(new TokenResponse("new-access-token", "new-refresh-token", 3600, "Bearer"), HttpStatus.OK));

            useCase.refreshToken(request, response);

            verify(restTemplateService).exchange(contains(TOKEN_PATH), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull());
        }

        @Test
        void when_empty_cookies_array_expect_token_endpoint_still_called() {
            when(request.getCookies()).thenReturn(new Cookie[]{});
            when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull()))
                    .thenReturn(new ResponseEntity<>(new TokenResponse("new-access-token", "new-refresh-token", 3600, "Bearer"), HttpStatus.OK));

            useCase.refreshToken(request, response);

            verify(restTemplateService).exchange(contains(TOKEN_PATH), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull());
        }

        @Test
        void when_other_cookies_present_but_no_refresh_token_expect_token_endpoint_still_called() {
            when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("session_id", "abc123")});
            when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull()))
                    .thenReturn(new ResponseEntity<>(new TokenResponse("new-access-token", "new-refresh-token", 3600, "Bearer"), HttpStatus.OK));

            useCase.refreshToken(request, response);

            verify(restTemplateService).exchange(contains(TOKEN_PATH), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull());
        }

        @Test
        void when_refresh_token_among_multiple_cookies_expect_new_tokens_set() {
            final Cookie[] cookies = {
                    new Cookie("session_id", "session123"),
                    new Cookie("refresh_token", "my-refresh-token"),
                    new Cookie("other", "value")
            };
            when(request.getCookies()).thenReturn(cookies);
            when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull()))
                    .thenReturn(new ResponseEntity<>(new TokenResponse("new-access-token", "new-refresh-token", 3600, "Bearer"), HttpStatus.OK));

            useCase.refreshToken(request, response);

            verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
        }
    }

    @Nested
    class Logout {

        @Test
        void when_logging_out_expect_access_and_refresh_token_cookies_cleared() {
            final ArgumentCaptor<String> cookieCaptor = ArgumentCaptor.forClass(String.class);

            useCase.logout(response);

            verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), cookieCaptor.capture());
            final String accessTokenCookie = cookieCaptor.getAllValues().get(0);
            final String refreshTokenCookie = cookieCaptor.getAllValues().get(1);
            assertThat(accessTokenCookie).contains("access_token=");
            assertThat(accessTokenCookie).contains("Max-Age=0");
            assertThat(refreshTokenCookie).contains("refresh_token=");
            assertThat(refreshTokenCookie).contains("Max-Age=0");
        }

        @Test
        void when_logging_out_expect_keycloak_logout_endpoint_called() {
            useCase.logout(response);

            verify(restTemplateService).exchange(
                    eq(KEYCLOAK_ISSUER_URI + LOGOUT_PATH),
                    eq(HttpMethod.POST),
                    isNull(),
                    isNull(),
                    eq(Void.class),
                    isNull()
            );
        }

        @Test
        void when_logging_out_expect_cookies_have_correct_security_attributes() {
            final ArgumentCaptor<String> cookieCaptor = ArgumentCaptor.forClass(String.class);

            useCase.logout(response);

            verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), cookieCaptor.capture());
            final String accessTokenCookie = cookieCaptor.getAllValues().get(0);
            final String refreshTokenCookie = cookieCaptor.getAllValues().get(1);
            assertThat(accessTokenCookie).contains("HttpOnly");
            assertThat(accessTokenCookie).contains("Secure");
            assertThat(accessTokenCookie).contains("SameSite=Strict");
            assertThat(accessTokenCookie).contains("Path=/");
            assertThat(refreshTokenCookie).contains("HttpOnly");
            assertThat(refreshTokenCookie).contains("Secure");
            assertThat(refreshTokenCookie).contains("SameSite=Strict");
            assertThat(refreshTokenCookie).contains("Path=public/auth/refresh");
        }
    }

    private String createJwtToken(final String givenName, final String familyName) {
        final String header = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes());
        final String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(
                ("{\"sub\":\"1234567890\",\"given_name\":\"" + givenName + "\",\"family_name\":\"" + familyName + "\",\"iat\":1516239022}").getBytes());
        final String signature = Base64.getUrlEncoder().withoutPadding().encodeToString("signature".getBytes());
        return header + "." + payload + "." + signature;
    }

    private String createJwtTokenWithOnlyGivenName(final String givenName) {
        final String header = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes());
        final String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(
                ("{\"sub\":\"1234567890\",\"given_name\":\"" + givenName + "\",\"iat\":1516239022}").getBytes());
        final String signature = Base64.getUrlEncoder().withoutPadding().encodeToString("signature".getBytes());
        return header + "." + payload + "." + signature;
    }

    private String createJwtTokenWithoutNames() {
        final String header = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes());
        final String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(
                "{\"sub\":\"1234567890\",\"iat\":1516239022}".getBytes());
        final String signature = Base64.getUrlEncoder().withoutPadding().encodeToString("signature".getBytes());
        return header + "." + payload + "." + signature;
    }
}
