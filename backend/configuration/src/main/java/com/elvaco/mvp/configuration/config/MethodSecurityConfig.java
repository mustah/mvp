package com.elvaco.mvp.configuration.config;

import java.util.Optional;

import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.security.TokenService;
import com.elvaco.mvp.web.security.AuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.authentication.InsufficientAuthenticationException;
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
    return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
      .filter(authentication -> authentication instanceof AuthenticationToken)
      .map(authentication -> (AuthenticationToken) authentication)
      .flatMap(authenticationToken -> tokenService.getToken(authenticationToken.getToken()))
      .orElseThrow(this::insufficientAuthenticationException);
  }

  private InsufficientAuthenticationException insufficientAuthenticationException() {
    return new InsufficientAuthenticationException("No authentication information available!");
  }
}
