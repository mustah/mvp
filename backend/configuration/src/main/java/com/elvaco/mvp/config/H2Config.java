package com.elvaco.mvp.config;

import javax.persistence.EntityManager;

import com.elvaco.mvp.repository.jpa.MeteringPointRepository;
import com.elvaco.mvp.repository.jpa.h2.H2MeteringPointRepository;

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

  @Bean
  MeteringPointRepository meteringPointRepository() {
    return new H2MeteringPointRepository(entityManager);
  }
}
