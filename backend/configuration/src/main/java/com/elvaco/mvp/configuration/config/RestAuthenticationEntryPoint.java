package com.elvaco.mvp.configuration.config;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private static final String WWW_AUTHENTICATE = "WWW-Authenticate";
  private static final String FORM_BASED = "FormBased";

  @Override
  public void commence(
    HttpServletRequest request,
    HttpServletResponse response,
    AuthenticationException authException
  ) throws IOException {
    response.setHeader(WWW_AUTHENTICATE, FORM_BASED);
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
  }
}
