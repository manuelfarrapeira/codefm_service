package org.web.codefm.domain.entity.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
@Generated
public class ErrorMessage {

  private final String param;

  private String message;
}
