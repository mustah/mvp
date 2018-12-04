package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.core.access.QuantityAccess;
import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.QuantityPresentationInformation;
import com.elvaco.mvp.core.unitconverter.UnitConverter;
import com.elvaco.mvp.core.util.LogicalMeterHelper;
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
  QuantityProviderRepository quantityProviderRepository(
    QuantityProviderJpaRepository quantityProviderJpaRepository
  ) {
    // this should only be used when reading/saving the initial quantities
    return new QuantityProviderRepository(quantityProviderJpaRepository);
  }

  @Bean
  QuantityProvider quantityProvider(QuantityProviderRepository quantityProviderRepository) {
    // we cannot use the domain model <-> entity mapper here, because of a circular dependency,
    // so we must simulate it both ways here
    Quantity.QUANTITIES.forEach(quantity ->
      quantityProviderRepository
        .findByName(quantity.name)
        .orElseGet(() -> quantityProviderRepository.save(
          QuantityEntity.builder()
            .displayUnit(quantity.presentationUnit())
            .name(quantity.name)
            .storageUnit(quantity.storageUnit)
            .seriesDisplayMode(quantity.seriesDisplayMode())
            .build()
        ))
    );

    var savedQuantities = quantityProviderRepository.findAllEntities()
      .stream()
      .map(quantityEntity -> new Quantity(
        quantityEntity.id,
        quantityEntity.name,
        new QuantityPresentationInformation(
          quantityEntity.displayUnit,
          quantityEntity.seriesDisplayMode
        ),
        quantityEntity.storageUnit
      ))
      .collect(toList());

    return new QuantityAccess(savedQuantities);
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
    return new MeasurementEntityMapper(unitConverter, quantityProvider, quantityEntityMapper);
  }
}
