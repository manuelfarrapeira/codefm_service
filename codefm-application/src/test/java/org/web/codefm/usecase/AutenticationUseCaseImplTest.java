package org.web.codefm.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Base64;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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

@ExtendWith(MockitoExtension.class)
class AutenticationUseCaseImplTest {

    @Mock
    RestTemplateService restTemplateService;

    @Mock
    HttpServletResponse response;

    @Mock
    HttpServletRequest request;

    @InjectMocks
    AutenticationUseCaseImpl useCase;

  private static final String KEYCLOAK_ISSUER_URI = "http://localhost:8080/realms/test";

  private static final String TOKEN_PATH = "/protocol/openid-connect/token";

  private static final String LOGOUT_PATH = "/protocol/openid-connect/logout";

  private static final String CLIENT_ID = "test-client";

    @BeforeEach
    void setUp() {
      ReflectionTestUtils.setField(useCase, "keycloakIssuerUri", KEYCLOAK_ISSUER_URI);
      ReflectionTestUtils.setField(useCase, "tokenPath", TOKEN_PATH);
      ReflectionTestUtils.setField(useCase, "logoutPath", LOGOUT_PATH);
      ReflectionTestUtils.setField(useCase, "clientId", CLIENT_ID);
    }

  @Test
  void loginSuccessfullyWithValidCredentials() {
    String username = "testuser";
    String password = "testpass";
    String authHeader = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    String accessToken = createJwtToken("John", "Doe");
    TokenResponse tokenResponse = new TokenResponse(accessToken, "refresh-token-123", 3600, "Bearer");

    when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull()))
        .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));

    LoginResponse result = useCase.login(authHeader, response);

    assertNotNull(result);
    assertEquals(accessToken, result.getAccessToken());
    assertEquals("John Doe", result.getUserName());
    verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
    }

    @Test
    void loginSetsAccessTokenCookieWithCorrectAttributes() {
      String username = "user";
      String password = "pass";
      String authHeader = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
      String accessToken = createJwtToken("Jane", "Smith");
      TokenResponse tokenResponse = new TokenResponse(accessToken, "refresh-token", 3600, "Bearer");

      when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull()))
          .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));

      ArgumentCaptor<String> cookieCaptor = ArgumentCaptor.forClass(String.class);

      useCase.login(authHeader, response);

      verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), cookieCaptor.capture());
      String accessTokenCookie = cookieCaptor.getAllValues().get(0);
      assertTrue(accessTokenCookie.contains("access_token=" + accessToken));
      assertTrue(accessTokenCookie.contains("HttpOnly"));
      assertTrue(accessTokenCookie.contains("Secure"));
      assertTrue(accessTokenCookie.contains("SameSite=Strict"));
      assertTrue(accessTokenCookie.contains("Max-Age=43200"));
      assertTrue(accessTokenCookie.contains("Path=/"));
    }

    @Test
    void loginSetsRefreshTokenCookieWithCorrectAttributes() {
      String username = "user";
      String password = "pass";
      String authHeader = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
      String accessToken = createJwtToken("Jane", "Smith");
      String refreshToken = "refresh-token-xyz";
      TokenResponse tokenResponse = new TokenResponse(accessToken, refreshToken, 3600, "Bearer");

      when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull()))
          .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));

      ArgumentCaptor<String> cookieCaptor = ArgumentCaptor.forClass(String.class);

      useCase.login(authHeader, response);

      verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), cookieCaptor.capture());
      String refreshTokenCookie = cookieCaptor.getAllValues().get(1);
      assertTrue(refreshTokenCookie.contains("refresh_token=" + refreshToken));
      assertTrue(refreshTokenCookie.contains("HttpOnly"));
      assertTrue(refreshTokenCookie.contains("Secure"));
      assertTrue(refreshTokenCookie.contains("SameSite=Strict"));
      assertTrue(refreshTokenCookie.contains("Max-Age=43200"));
      assertTrue(refreshTokenCookie.contains("Path=public/auth/refresh"));
    }

    @Test
    void loginThrowsUserNotFoundWhenCredentialsAreInvalid() {
      String username = "wronguser";
      String password = "wrongpass";
      String authHeader = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());

      when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull()))
          .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

      assertThrows(UserNotFound.class, () -> useCase.login(authHeader, response));
    }

  @Test
  void loginExtractsUserNameFromTokenWithBothGivenAndFamilyName() {
    String authHeader = "Basic " + Base64.getEncoder().encodeToString(("user:pass").getBytes());
    String accessToken = createJwtToken("Alice", "Wonder");
    TokenResponse tokenResponse = new TokenResponse(accessToken, "refresh", 3600, "Bearer");

    when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull()))
        .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));

    LoginResponse result = useCase.login(authHeader, response);

    assertEquals("Alice Wonder", result.getUserName());
  }

  @Test
  void loginHandlesTokenWithOnlyGivenName() {
    String authHeader = "Basic " + Base64.getEncoder().encodeToString(("user:pass").getBytes());
    String accessToken = createJwtTokenWithOnlyGivenName("SingleName");
    TokenResponse tokenResponse = new TokenResponse(accessToken, "refresh", 3600, "Bearer");

    when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull()))
        .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));

    LoginResponse result = useCase.login(authHeader, response);

    assertEquals("SingleName ", result.getUserName());
  }

  @Test
  void loginHandlesTokenWithMissingNames() {
    String authHeader = "Basic " + Base64.getEncoder().encodeToString(("user:pass").getBytes());
    String accessToken = createJwtTokenWithoutNames();
    TokenResponse tokenResponse = new TokenResponse(accessToken, "refresh", 3600, "Bearer");

    when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull()))
        .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));

    LoginResponse result = useCase.login(authHeader, response);

    assertEquals(" ", result.getUserName());
  }

  @Test
  void refreshTokenSuccessfullyWithValidRefreshToken() {
    String refreshTokenValue = "valid-refresh-token";
    Cookie refreshTokenCookie = new Cookie("refresh_token", refreshTokenValue);
        Cookie[] cookies = {refreshTokenCookie};
    TokenResponse tokenResponse = new TokenResponse("new-access-token", "new-refresh-token", 3600, "Bearer");

        when(request.getCookies()).thenReturn(cookies);
    when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull()))
        .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));

    useCase.refreshToken(request, response);

    verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
    }

    @Test
    void refreshTokenWithNoCookies() {
        when(request.getCookies()).thenReturn(null);
      TokenResponse tokenResponse = new TokenResponse("new-access-token", "new-refresh-token", 3600, "Bearer");

      when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull()))
          .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));

      useCase.refreshToken(request, response);

      verify(restTemplateService).exchange(contains(TOKEN_PATH), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull());
    }

  @Test
  void refreshTokenWithEmptyCookiesArray() {
    Cookie[] cookies = {};

    when(request.getCookies()).thenReturn(cookies);
    TokenResponse tokenResponse = new TokenResponse("new-access-token", "new-refresh-token", 3600, "Bearer");

    when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull()))
        .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));

    useCase.refreshToken(request, response);

    verify(restTemplateService).exchange(contains(TOKEN_PATH), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull());
    }

    @Test
    void refreshTokenWithOtherCookiesButNoRefreshToken() {
      Cookie otherCookie = new Cookie("session_id", "abc123");
      Cookie[] cookies = {otherCookie};

      when(request.getCookies()).thenReturn(cookies);
      TokenResponse tokenResponse = new TokenResponse("new-access-token", "new-refresh-token", 3600, "Bearer");

      when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull()))
          .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));

      useCase.refreshToken(request, response);

      verify(restTemplateService).exchange(contains(TOKEN_PATH), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull());
    }

  @Test
  void refreshTokenFindsRefreshTokenAmongMultipleCookies() {
    String refreshTokenValue = "my-refresh-token";
    Cookie sessionCookie = new Cookie("session_id", "session123");
    Cookie refreshTokenCookie = new Cookie("refresh_token", refreshTokenValue);
    Cookie otherCookie = new Cookie("other", "value");
    Cookie[] cookies = {sessionCookie, refreshTokenCookie, otherCookie};
    TokenResponse tokenResponse = new TokenResponse("new-access-token", "new-refresh-token", 3600, "Bearer");

    when(request.getCookies()).thenReturn(cookies);
    when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull()))
        .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));

    useCase.refreshToken(request, response);

        verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
    }

  @Test
  void logoutClearsAccessAndRefreshTokenCookies() {
    ArgumentCaptor<String> cookieCaptor = ArgumentCaptor.forClass(String.class);

    useCase.logout(response);

    verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), cookieCaptor.capture());
    String accessTokenCookie = cookieCaptor.getAllValues().get(0);
    String refreshTokenCookie = cookieCaptor.getAllValues().get(1);

    assertTrue(accessTokenCookie.contains("access_token="));
    assertTrue(accessTokenCookie.contains("Max-Age=0"));
    assertTrue(refreshTokenCookie.contains("refresh_token="));
    assertTrue(refreshTokenCookie.contains("Max-Age=0"));
  }

  @Test
  void logoutCallsKeycloakLogoutEndpoint() {
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
  void logoutSetsCorrectCookieAttributes() {
    ArgumentCaptor<String> cookieCaptor = ArgumentCaptor.forClass(String.class);

    useCase.logout(response);

    verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), cookieCaptor.capture());
    String accessTokenCookie = cookieCaptor.getAllValues().get(0);
    String refreshTokenCookie = cookieCaptor.getAllValues().get(1);

    assertTrue(accessTokenCookie.contains("HttpOnly"));
    assertTrue(accessTokenCookie.contains("Secure"));
    assertTrue(accessTokenCookie.contains("SameSite=Strict"));
    assertTrue(accessTokenCookie.contains("Path=/"));

    assertTrue(refreshTokenCookie.contains("HttpOnly"));
    assertTrue(refreshTokenCookie.contains("Secure"));
    assertTrue(refreshTokenCookie.contains("SameSite=Strict"));
    assertTrue(refreshTokenCookie.contains("Path=public/auth/refresh"));
  }

  @Test
  void loginDecodesBase64EncodedCredentialsCorrectly() {
    String username = "user@example.com";
    String password = "p@ssw0rd!";
    String authHeader = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    String accessToken = createJwtToken("Test", "User");
    TokenResponse tokenResponse = new TokenResponse(accessToken, "refresh", 3600, "Bearer");

    when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull()))
        .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));

    LoginResponse result = useCase.login(authHeader, response);

    assertNotNull(result);
    assertEquals("Test User", result.getUserName());
  }

  @Test
  void loginHandlesPasswordWithColonCharacter() {
    String username = "testuser";
    String password = "pass:word:123";
    String authHeader = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    String accessToken = createJwtToken("Test", "User");
    TokenResponse tokenResponse = new TokenResponse(accessToken, "refresh", 3600, "Bearer");

    when(restTemplateService.exchange(anyString(), eq(HttpMethod.POST), any(), any(), eq(TokenResponse.class), isNull()))
        .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));

    LoginResponse result = useCase.login(authHeader, response);

    assertNotNull(result);
  }

  private String createJwtToken(String givenName, String familyName) {
    String header = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes());
    String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(
        ("{\"sub\":\"1234567890\",\"given_name\":\"" + givenName + "\",\"family_name\":\"" + familyName
            + "\",\"iat\":1516239022}").getBytes()
    );
    String signature = Base64.getUrlEncoder().withoutPadding().encodeToString("signature".getBytes());
    return header + "." + payload + "." + signature;
  }

  private String createJwtTokenWithOnlyGivenName(String givenName) {
    String header = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes());
    String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(
        ("{\"sub\":\"1234567890\",\"given_name\":\"" + givenName + "\",\"iat\":1516239022}").getBytes()
    );
    String signature = Base64.getUrlEncoder().withoutPadding().encodeToString("signature".getBytes());
    return header + "." + payload + "." + signature;
  }

  private String createJwtTokenWithoutNames() {
    String header = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes());
    String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(
        "{\"sub\":\"1234567890\",\"iat\":1516239022}".getBytes()
    );
    String signature = Base64.getUrlEncoder().withoutPadding().encodeToString("signature".getBytes());
    return header + "." + payload + "." + signature;
  }
}

