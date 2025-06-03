package org.web.codefm.api.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestUtils {

  public static final HttpServletRequest getCurrentRequest() {
    return Optional.ofNullable((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
        .map(ServletRequestAttributes::getRequest)
        .orElse(null);
  }

}
