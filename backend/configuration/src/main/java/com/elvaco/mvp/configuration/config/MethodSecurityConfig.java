package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.security.TokenService;
import com.elvaco.mvp.web.security.AuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Configuration
class MethodSecurityConfig {

  private final TokenService tokenService;

  @Autowired
  MethodSecurityConfig(TokenService tokenService) {
    this.tokenService = tokenService;
  }

  @Bean
  @Scope(value = SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
  AuthenticatedUser currentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof AuthenticationToken) {
      return tokenService
        .getToken(((AuthenticationToken) authentication).getToken())
        .orElseThrow(this::insufficientAuthenticationException);
    } else if (authentication instanceof UsernamePasswordAuthenticationToken) {
      return (AuthenticatedUser) authentication.getPrincipal();
    }
    throw insufficientAuthenticationException();
  }

  private InsufficientAuthenticationException insufficientAuthenticationException() {
    return new InsufficientAuthenticationException("No authentication information available!");
  }
}
