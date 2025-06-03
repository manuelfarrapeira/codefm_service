package org.web.codefm.api.mapper;

import lombok.experimental.UtilityClass;
import org.web.codefm.domain.exception.BaseException;
import org.web.codefm.domain.exception.ErrorMessageBaseException;
import org.web.codefm.domain.exception.ListErrorMessageBaseException;
import org.web.codefm.model.DetailDTO;
import org.web.codefm.model.ErrorResponseDTO;

import java.util.stream.Collectors;

@UtilityClass
public class ErrorResponseMapper {

  public <T extends BaseException> ErrorResponseDTO toDTO(final T exception) {

    final ErrorResponseDTO errorDTO = new ErrorResponseDTO();
    errorDTO.setCode(exception.getErrorCodeEnum().getCode());
    errorDTO.setDescription(exception.getErrorCodeEnum().getDescription());

    if (exception instanceof ErrorMessageBaseException) {
      errorDTO.setDetail(((ErrorMessageBaseException) exception).getErrorDescription());
    } else if (exception instanceof ListErrorMessageBaseException) {
      errorDTO.setDetails(((ListErrorMessageBaseException) exception).getErrors().stream()
          .map(ex -> new DetailDTO().field(ex.getParam()).reason(ex.getMessage())).collect(
              Collectors.toList()));
    }
    return errorDTO;
  }
}
