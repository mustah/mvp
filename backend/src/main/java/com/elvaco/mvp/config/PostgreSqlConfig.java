package com.elvaco.mvp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

@PostgreSql
@Configuration
public class PostgreSqlConfig {

  /**
   * Delegating to in-memory version of the {@link UserDetailsService} for now, until we have an
   * ACL implementation of
   * user management in the DB.
   */
  @Bean
  public UserDetailsService userDetailsService() {
    return new InMemoryConfig().userDetailsService();
  }
}