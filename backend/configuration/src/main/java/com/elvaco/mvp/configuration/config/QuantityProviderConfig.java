package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.core.access.QuantityAccess;
import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.unitconverter.UnitConverter;
import com.elvaco.mvp.database.entity.meter.QuantityEntity;
import com.elvaco.mvp.database.repository.access.QuantityProviderRepository;
import com.elvaco.mvp.database.repository.jpa.QuantityProviderJpaRepository;
import com.elvaco.mvp.database.repository.mappers.GatewayWithMetersMapper;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterEntityMapper;
import com.elvaco.mvp.database.repository.mappers.MeasurementEntityMapper;
import com.elvaco.mvp.database.repository.mappers.MeterDefinitionEntityMapper;
import com.elvaco.mvp.database.repository.mappers.QuantityEntityMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.stream.Collectors.toList;

@Configuration
class QuantityProviderConfig {

  @Bean
  QuantityProviderRepository initialQuantityProviderRepository(
    QuantityProviderJpaRepository quantityProviderJpaRepository
  ) {
    return new QuantityProviderRepository(quantityProviderJpaRepository);
  }

  /**
   * We cannot use the domain model <-> entity mapper before having a real QuantityProvider,
   * because of a circular dependency. We avoid the circle by using a separate JPA repository and
   * do the mapping ourselves.
   *
   * @param initialQuantityProviderRepository
   *
   * @return QuantityProvider
   */
  @Bean
  QuantityProvider quantityProvider(QuantityProviderRepository initialQuantityProviderRepository) {
    Quantity.QUANTITIES.forEach(quantity ->
      initialQuantityProviderRepository
        .findByName(quantity.name)
        .orElseGet(() -> initialQuantityProviderRepository.save(
          QuantityEntity.builder()
            .name(quantity.name)
            .storageUnit(quantity.storageUnit)
            .build()
        ))
    );

    var savedQuantities = initialQuantityProviderRepository.findAllEntities()
      .stream()
      .map(quantityEntity -> new Quantity(
        quantityEntity.id,
        quantityEntity.name,
        quantityEntity.storageUnit
      ))
      .collect(toList());

    return new QuantityAccess(savedQuantities);
  }

  @Bean
  QuantityEntityMapper quantityEntityMapper(QuantityProvider quantityProvider) {
    return new QuantityEntityMapper(quantityProvider);
  }

  @Bean
  MeterDefinitionEntityMapper meterDefinitionEntityMapper(
    QuantityEntityMapper quantityEntityMapper
  ) {
    return new MeterDefinitionEntityMapper(quantityEntityMapper);
  }

  @Bean
  LogicalMeterEntityMapper logicalMeterEntityMapper(
    MeterDefinitionEntityMapper meterDefinitionEntityMapper
  ) {
    return new LogicalMeterEntityMapper(meterDefinitionEntityMapper);
  }

  @Bean
  GatewayWithMetersMapper gatewayWithMetersMapper(
    LogicalMeterEntityMapper logicalMeterEntityMapper
  ) {
    return new GatewayWithMetersMapper(logicalMeterEntityMapper);
  }

  @Bean
  MeasurementEntityMapper measurementEntityMapper(
    UnitConverter unitConverter,
    QuantityProvider quantityProvider,
    QuantityEntityMapper quantityEntityMapper
  ) {
    return new MeasurementEntityMapper(unitConverter,
      quantityProvider, quantityEntityMapper
    );
  }
}
