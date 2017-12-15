package com.elvaco.mvp.config;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

@H2
@Configuration
class H2Config {

  private final EntityManager entityManager;

  @Autowired
  H2Config(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Bean
  UserDetailsManager userDetailsService() {
    return new InMemoryUserDetailsManager();
  }
}
