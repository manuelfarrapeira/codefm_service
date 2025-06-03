package org.web.codefm.domain.entity.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class ErrorMessage {

  private final String param;

  private String message;
}
