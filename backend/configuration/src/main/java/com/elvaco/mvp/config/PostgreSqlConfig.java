package com.elvaco.mvp.config;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

@PostgreSql
@Configuration
class PostgreSqlConfig {

  private final EntityManager entityManager;

  @Autowired
  PostgreSqlConfig(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  /**
   * Delegating to h2 version of the {@link UserDetailsService} for now, until we have an
   * ACL implementation of user management in the DB.
   */
  @Bean
  UserDetailsService userDetailsService() {
    return new H2Config(entityManager).userDetailsService();
  }
}
