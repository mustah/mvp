package com.elvaco.mvp.configuration.config;

import javax.persistence.EntityManager;

import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterQueryDslJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Order(1)
@Configuration
class JpaRepositoryProviderConfig {

  private final EntityManager entityManager;

  @Autowired
  JpaRepositoryProviderConfig(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Bean
  LogicalMeterJpaRepository logicalMeterJpaRepository() {
    return new LogicalMeterQueryDslJpaRepository(entityManager);
  }
}
