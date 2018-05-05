package com.elvaco.mvp.web.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import static java.util.Objects.requireNonNull;

public class AuthenticationToken extends AbstractAuthenticationToken {

  private final String token;

  public AuthenticationToken(String token, Object details) {
    super(null);
    this.token = requireNonNull(token, "Token cannot be null");
    this.setDetails(details);
  }

  public String getToken() {
    return token;
  }

  @Override
  public boolean isAuthenticated() {
    return token != null;
  }

  @Override
  public Object getCredentials() {
    return token;
  }

  @Override
  public Object getPrincipal() {
    return null;
  }

  @Override
  public String toString() {
    return "AuthenticationToken{" + "token='" + token + '\'' + '}';
  }
}
