package com.elvaco.mvp.web.api;

import java.util.Map;

import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

@Component
class ExtendedErrorAttributes implements ErrorAttributes {
  private static final String INTERNAL_ERROR_MESSAGE
    = "Internal server error, please contact support.";

  @Override
  public Map<String, Object> getErrorAttributes(
    WebRequest webRequest, boolean includeStackTrace
  ) {
    return Map.of(
      "message", INTERNAL_ERROR_MESSAGE,
      "status", HttpStatus.INTERNAL_SERVER_ERROR.value()
    );
  }

  @Override
  public Throwable getError(WebRequest webRequest) {
    return null;
  }
}
