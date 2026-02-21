package org.web.codefm.api.mapper;

import org.springframework.stereotype.Component;
import org.web.codefm.domain.exception.BaseException;
import org.web.codefm.domain.exception.ErrorMessageBaseException;
import org.web.codefm.domain.exception.ListErrorMessageBaseException;
import org.web.codefm.model.DetailDTO;
import org.web.codefm.model.ErrorResponseDTO;

@Component
public class ErrorResponseMapper {

    // Removed MessageSource as messages are now pre-translated

    public <T extends BaseException> ErrorResponseDTO toDTO(final T exception) { // Removed MessageSource and Locale parameters

        final ErrorResponseDTO errorDTO = new ErrorResponseDTO();
        errorDTO.setCode(exception.getErrorCodeEnum().getCode());
        errorDTO.setDescription(exception.getErrorCodeEnum().getDescription());

        if (exception instanceof ErrorMessageBaseException errMsgEx) {
            errorDTO.setDetail(errMsgEx.getErrorDescription());
        } else if (exception instanceof ListErrorMessageBaseException listErrMsgEx) {
            errorDTO.setDetails(listErrMsgEx.getErrors().stream()
                    .map(ex -> new DetailDTO().field(ex.getParam()).reason(ex.getMessage())) // Directly use ex.getMessage()
                    .toList());
        }
        return errorDTO;
    }
}
