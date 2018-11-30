package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.core.access.QuantityAccess;
import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.unitconverter.UnitConverter;
import com.elvaco.mvp.core.util.LogicalMeterHelper;
import com.elvaco.mvp.database.repository.mappers.GatewayWithMetersMapper;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterEntityMapper;
import com.elvaco.mvp.database.repository.mappers.MeasurementEntityMapper;
import com.elvaco.mvp.database.repository.mappers.MeterDefinitionEntityMapper;
import com.elvaco.mvp.database.repository.mappers.QuantityEntityMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class QuantityProviderConfig {

  @Bean
  QuantityProvider quantityProvider() {
    //FIXME: This should not be a singleton
    return QuantityAccess.singleton();
  }

  @Bean
  LogicalMeterHelper logicalMeterHelper(QuantityProvider quantityProvider) {
    return new LogicalMeterHelper(quantityProvider);
  }

  @Bean
  QuantityEntityMapper quantityEntityMapper(QuantityProvider quantityProvider) {
    return new QuantityEntityMapper(quantityProvider);
  }

  @Bean
  MeterDefinitionEntityMapper meterDefinitionEntityMapper(
    QuantityEntityMapper quantityEntityMapper,
    QuantityProvider quantityProvider
  ) {
    return new MeterDefinitionEntityMapper(quantityEntityMapper, quantityProvider);
  }

  @Bean
  LogicalMeterEntityMapper logicalMeterEntityMapper(MeterDefinitionEntityMapper meterDefinitionEntityMapper) {
    return new LogicalMeterEntityMapper(meterDefinitionEntityMapper);
  }

  @Bean
  GatewayWithMetersMapper gatewayWithMetersMapper(LogicalMeterEntityMapper logicalMeterEntityMapper) {
    return new GatewayWithMetersMapper(logicalMeterEntityMapper);
  }

  @Bean
  MeasurementEntityMapper measurementEntityMapper(
    UnitConverter unitConverter,
    QuantityProvider quantityProvider,
    QuantityEntityMapper quantityEntityMapper
  ) {
    return new MeasurementEntityMapper(unitConverter, quantityProvider, quantityEntityMapper);
  }
}
