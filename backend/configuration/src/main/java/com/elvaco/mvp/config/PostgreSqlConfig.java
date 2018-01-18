package com.elvaco.mvp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.UserDetailsManager;

@PostgreSql
@Configuration
class PostgreSqlConfig {

  /**
   * Delegating to h2 version of the {@link UserDetailsService} for now, until we have an
   * ACL implementation of user management in the DB.
   */
  @Bean
  UserDetailsManager userDetailsService() {
    return new H2Config().userDetailsService();
  }
}
