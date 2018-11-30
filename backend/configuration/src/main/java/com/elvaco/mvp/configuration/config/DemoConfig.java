package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.configuration.bootstrap.demo.DemoDataHelper;
import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.database.repository.mappers.QuantityEntityMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class DemoConfig {

  @Bean
  DemoDataHelper demoDataHelper(
    QuantityProvider quantityProvider,
    QuantityEntityMapper quantityEntityMapper
  ) {
    return new DemoDataHelper(quantityProvider, quantityEntityMapper);
  }
}
