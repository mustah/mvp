package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.security.TokenService;
import com.elvaco.mvp.web.security.AuthenticationToken;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@RequiredArgsConstructor
@Configuration
class MethodSecurityConfig {

  private static final InsufficientAuthenticationException INSUFFICIENT_AUTHENTICATION_EXCEPTION =
    new InsufficientAuthenticationException("No authentication information available!");

  private final TokenService tokenService;

  @Bean
  @Scope(value = SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
  AuthenticatedUser currentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof AuthenticationToken) {
      return tokenService.getToken((String) authentication.getCredentials())
        .orElseThrow(() -> INSUFFICIENT_AUTHENTICATION_EXCEPTION);
    } else if (authentication instanceof UsernamePasswordAuthenticationToken) {
      return (AuthenticatedUser) authentication.getPrincipal();
    }
    throw INSUFFICIENT_AUTHENTICATION_EXCEPTION;
  }
}
