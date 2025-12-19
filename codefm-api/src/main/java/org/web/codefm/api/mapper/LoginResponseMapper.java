package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.session.LoginResponse;
import org.web.codefm.model.LoginResponseDTO;

/**
 * Mapper for converting between LoginResponse domain model and LoginResponseDTO. Maps authentication response data for API layer.
 */
@Mapper(componentModel = "spring")
public interface LoginResponseMapper {

  /**
   * Converts LoginResponse domain model to LoginResponseDTO.
   *
   * @param loginResponse The domain model containing authentication data
   * @return LoginResponseDTO for API response
   */
  LoginResponseDTO toDTO(LoginResponse loginResponse);
}

