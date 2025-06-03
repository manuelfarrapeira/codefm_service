package org.web.codefm.api.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.web.codefm.api.mapper.ErrorResponseMapper;
import org.web.codefm.domain.exception.BaseException;
import org.web.codefm.model.ErrorResponseDTO;

import java.util.Objects;

@Slf4j
@ControllerAdvice
public class RestExceptionHandler {


    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponseDTO> mapperException(final Exception ex) {
        log.error("Error response: " + ex.getMessage(), ex);

        final ExceptionStatusEnum exeptionEnum = ExceptionStatusEnum.getExceptionEnum(ex.getClass());
        final HttpStatus status = (Objects.isNull(exeptionEnum)) ? HttpStatus.INTERNAL_SERVER_ERROR : exeptionEnum.getStatus();

        final ErrorResponseDTO errorDTO = ErrorResponseMapper.toDTO((BaseException) ex);

        return ResponseEntity.status(status).body(errorDTO);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> mapperGenericException(final AccessDeniedException ex) {
        log.error("Error de acceso: " + ex.getMessage(), ex);

        ErrorResponseDTO errorDTO = new ErrorResponseDTO();
        errorDTO.setDetail(ex.getMessage());
        errorDTO.setCode("403");
        errorDTO.setDescription("FORBIDDEN");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorDTO);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> mapperGenericException(final Exception ex) {
        log.error("Error general no controlado: " + ex.getMessage(), ex);

        ErrorResponseDTO errorDTO = new ErrorResponseDTO();
        errorDTO.setDetail(ex.getMessage());
        errorDTO.setCode("1000");
        errorDTO.setDescription("INTERNAL_SERVER_ERROR");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDTO);
    }




}
