package com.elvaco.mvp.config;

import com.elvaco.mvp.core.usecase.Users;
import com.elvaco.mvp.mapper.UserMapper;
import com.elvaco.mvp.security.OrganisationPermissionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;

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
  PermissionEvaluator permissionEvaluator() {
    return new OrganisationPermissionEvaluator(users, userMapper);
  }

}
