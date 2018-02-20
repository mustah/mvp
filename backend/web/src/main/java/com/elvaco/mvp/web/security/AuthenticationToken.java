package com.elvaco.mvp.web.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class AuthenticationToken extends AbstractAuthenticationToken {

  private final String token;

  public AuthenticationToken(String token) {
    super(null);
    this.token = token;
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
