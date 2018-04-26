package com.elvaco.mvp.web.util;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;

import lombok.experimental.UtilityClass;

import static com.elvaco.mvp.web.util.Constants.AUTHORIZATION;
import static com.elvaco.mvp.web.util.Constants.BEARER;

@UtilityClass
public class RequestHelper {

  public static Optional<String> bearerTokenFrom(String requestHeader) {
    return Optional.ofNullable(requestHeader)
      .map(header -> header.replace(BEARER, ""));
  }

  public static Optional<String> bearerTokenFrom(HttpServletRequest request) {
    return bearerTokenFrom(request.getHeader(AUTHORIZATION));
  }
}
