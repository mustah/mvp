package com.elvaco.mvp.config;

import com.elvaco.mvp.core.security.MvpPrincipal;
import com.elvaco.mvp.core.usecase.Users;
import com.elvaco.mvp.mapper.UserMapper;
import com.elvaco.mvp.security.OrganisationPermissionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

@Configuration
class MethodSecurityConfig {

  private final Users users;
  private final UserMapper userMapper;

  @Autowired
  MethodSecurityConfig(Users users, UserMapper userMapper) {
    this.users = users;
    this.userMapper = userMapper;
  }

  @Bean
  @Scope(value = SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
  MvpPrincipal currentPrincipal() {
    return (MvpPrincipal) SecurityContextHolder.getContext()
      .getAuthentication()
      .getPrincipal();
  }

  @Bean
  PermissionEvaluator permissionEvaluator() {
    return new OrganisationPermissionEvaluator(users, userMapper);
  }

}
