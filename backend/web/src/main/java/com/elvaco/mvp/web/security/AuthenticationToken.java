package com.elvaco.mvp.web.security;

import com.elvaco.mvp.core.security.AuthenticatedUser;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;

import static java.util.Objects.requireNonNull;

public class AuthenticationToken extends AbstractAuthenticationToken {

  private final String token;
  private final Object principal;

  private AuthenticationToken(String token, Object details) {
    super(((MvpUserDetails) details).getAuthorities());
    this.token = requireNonNull(token, "Token cannot be null");
    this.principal = details;
    this.setDetails(details);
    this.setAuthenticated(true);
  }

  public static Authentication from(AuthenticatedUser user) {
    return new AuthenticationToken(user.getToken(), user);
  }

  @Override
  public Object getCredentials() {
    return token;
  }

  @Override
  public Object getPrincipal() {
    return principal;
  }

  @Override
  public String toString() {
    return "AuthenticationToken{" + "token='" + token + '\'' + '}';
  }
}
