package com.elvaco.mvp.config;

import javax.persistence.EntityManager;

import com.elvaco.mvp.repository.jpa.MeteringPointRepository;
import com.elvaco.mvp.repository.jpa.postgresql.PostgreSqlMeteringPointRepository;

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
  public UserDetailsService userDetailsService() {
    return new H2Config(entityManager).userDetailsService();
  }

  @Bean
  public MeteringPointRepository meteringPointRepository() {
    return new PostgreSqlMeteringPointRepository(entityManager);
  }
}
