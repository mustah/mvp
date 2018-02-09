package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.core.security.AuthenticatedUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Configuration
class MethodSecurityConfig {

  @Bean
  @Scope(value = SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
  AuthenticatedUser currentUser() {
    Authentication authentication = SecurityContextHolder.getContext()
      .getAuthentication();
    if (authentication == null) {
      throw new InsufficientAuthenticationException("No authentication information available!");
    }
    return (AuthenticatedUser) authentication.getPrincipal();
  }
}
