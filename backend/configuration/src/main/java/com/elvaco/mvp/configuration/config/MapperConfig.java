package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.web.mapper.MeterDefinitionDtoMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class MapperConfig {
  @Bean
  public MeterDefinitionDtoMapper meterDefinitionDtoMapper(QuantityProvider quantityProvider) {
    return new MeterDefinitionDtoMapper(quantityProvider);
  }
}
