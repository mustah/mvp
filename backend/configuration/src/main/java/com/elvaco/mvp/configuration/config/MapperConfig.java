package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.core.access.MediumProvider;
import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.database.repository.mappers.DisplayQuantityEntityMapper;
import com.elvaco.mvp.database.repository.mappers.MediumEntityMapper;
import com.elvaco.mvp.database.repository.mappers.MeterDefinitionEntityMapper;
import com.elvaco.mvp.web.mapper.MeterDefinitionDtoMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class MapperConfig {
  @Bean
  public MeterDefinitionDtoMapper meterDefinitionDtoMapper(QuantityProvider quantityProvider) {
    return new MeterDefinitionDtoMapper(quantityProvider);
  }

  @Bean
  MediumEntityMapper mediumEntityMapper(MediumProvider mediumProvider) {
    return new MediumEntityMapper(mediumProvider);
  }

  @Bean
  MeterDefinitionEntityMapper meterDefinitionEntityMapper(
    DisplayQuantityEntityMapper displayQuantityEntityMapper,
    MediumEntityMapper mediumEntityMapper
  ) {
    return new MeterDefinitionEntityMapper(
      mediumEntityMapper,
      displayQuantityEntityMapper
    );
  }
}
