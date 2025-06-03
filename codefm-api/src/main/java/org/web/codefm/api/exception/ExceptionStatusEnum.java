package org.web.codefm.api.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.web.codefm.domain.exception.UserNotFound;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ExceptionStatusEnum {

  USER_NOT_FOUND(UserNotFound.class, HttpStatus.NOT_FOUND),
  BAD_REQUEST(UserNotFound.class, HttpStatus.BAD_REQUEST);

  private final Class<?> exceptionClazz;

  private final HttpStatus status;

  public static <T extends Throwable> ExceptionStatusEnum getExceptionEnum(final Class<T> obj) {
    return Arrays.stream(ExceptionStatusEnum.values())
        .filter(ex -> (obj.equals(ex.getExceptionClazz()))).findFirst().orElse(null);
  }

}
