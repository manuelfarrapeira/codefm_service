package org.web.codefm.domain.exception;

import lombok.Getter;


@Getter
public enum ErrorCodeEnum {

  GENERIC_ERROR("1000", "GENERIC_ERROR"),
  PARSER_ERROR("1001", "PARSER_ERROR"),
  RESOURCE_NOT_FOUND("1003", "RESOURCE_NOT_FOUND"),
  RESOURCE_FORBIDDEN("1004", "RESOURCE_FORBIDDEN"),
  UNEXPECTED_ARGUMENT("1005", "UNEXPECTED_ARGUMENT");

  private final String code;

  private final String description;

  ErrorCodeEnum(final String code, final String description) {
    this.code = code;
    this.description = description;
  }
}
