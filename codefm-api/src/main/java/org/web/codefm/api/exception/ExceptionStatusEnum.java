package org.web.codefm.api.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.web.codefm.domain.exception.UserNotFound;
import org.web.codefm.domain.exception.teachernotebook.ClassValidationException;
import org.web.codefm.domain.exception.teachernotebook.SchoolForbiddenException;
import org.web.codefm.domain.exception.teachernotebook.SchoolNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.SchoolValidationException;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ExceptionStatusEnum {

  USER_NOT_FOUND(UserNotFound.class, HttpStatus.NOT_FOUND),
  VALIDATION_ERROR(SchoolValidationException.class, HttpStatus.BAD_REQUEST),
    CLASS_VALIDATION_ERROR(ClassValidationException.class, HttpStatus.BAD_REQUEST),
  SCHOOL_NOT_FOUND(SchoolNotFoundException.class, HttpStatus.NOT_FOUND),
  SCHOOL_FORBIDDEN(SchoolForbiddenException.class, HttpStatus.FORBIDDEN);

  private final Class<?> exceptionClazz;

  private final HttpStatus status;

  public static <T extends Throwable> ExceptionStatusEnum getExceptionEnum(final Class<T> obj) {
    return Arrays.stream(ExceptionStatusEnum.values())
        .filter(ex -> (obj.equals(ex.getExceptionClazz()))).findFirst().orElse(null);
  }

}
