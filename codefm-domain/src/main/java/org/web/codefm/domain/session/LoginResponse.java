package org.web.codefm.domain.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response object containing authentication information after successful login. Includes the JWT access token and user's full name.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

  /**
   * JWT access token for API authentication.
   */
  private String accessToken;

  /**
   * User's full name extracted from the JWT token.
   */
  private String userName;
}

