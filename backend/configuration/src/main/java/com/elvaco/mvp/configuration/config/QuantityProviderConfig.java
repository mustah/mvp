package com.elvaco.mvp.configuration.config;

import java.util.List;

import com.elvaco.mvp.configuration.bootstrap.production.ProductionDataProvider;
import com.elvaco.mvp.core.access.MediumAccess;
import com.elvaco.mvp.core.access.MediumProvider;
import com.elvaco.mvp.core.access.QuantityAccess;
import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.access.SystemMeterDefinitionAccess;
import com.elvaco.mvp.core.access.SystemMeterDefinitionProvider;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.unitconverter.UnitConverter;
import com.elvaco.mvp.database.entity.meter.MediumEntity;
import com.elvaco.mvp.database.entity.meter.QuantityEntity;
import com.elvaco.mvp.database.repository.access.QuantityProviderRepository;
import com.elvaco.mvp.database.repository.jpa.DisplayQuantityJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MediumJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeterDefinitionJpaRepository;
import com.elvaco.mvp.database.repository.jpa.QuantityProviderJpaRepository;
import com.elvaco.mvp.database.repository.mappers.DisplayQuantityEntityMapper;
import com.elvaco.mvp.database.repository.mappers.GatewayWithMetersMapper;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterEntityMapper;
import com.elvaco.mvp.database.repository.mappers.MeasurementEntityMapper;
import com.elvaco.mvp.database.repository.mappers.MediumEntityMapper;
import com.elvaco.mvp.database.repository.mappers.MeterDefinitionEntityMapper;
import com.elvaco.mvp.database.repository.mappers.QuantityEntityMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Configuration
@RequiredArgsConstructor
class QuantityProviderConfig {

  private final MediumJpaRepository mediumJpaRepository;
  private final MeterDefinitionJpaRepository meterDefinitionJpaRepository;
  private final DisplayQuantityJpaRepository displayQuantityJpaRepository;

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
  SystemMeterDefinitionProvider meterDefinitionProvider(
    ProductionDataProvider productionDataProvider,
    MeterDefinitionEntityMapper meterDefinitionEntityMapper
  ) {
    List<MeterDefinition> meterDefinitions =
      productionDataProvider.meterDefinitions()
        .stream()
        .map(meterDefinitionEntityMapper::toEntity)
        .map(meterDefinition ->
          meterDefinitionJpaRepository.findByMediumAndOrganisationIsNull(meterDefinition.medium)
            .orElseGet(() -> meterDefinitionJpaRepository.save(meterDefinition)))
        .peek(meterDefinitionEntity ->
          meterDefinitionEntity.quantities = meterDefinitionEntity.quantities.stream()
            .peek(
              displayQuantityEntity ->
                displayQuantityEntity.pk.meterDefinitionId = meterDefinitionEntity.id
            )
            .map(displayQuantityJpaRepository::save)
            .collect(toSet()))
        .map(meterDefinitionEntityMapper::toDomainModel)
        .collect(toList());

    return new SystemMeterDefinitionAccess(meterDefinitions);
  }

  @Bean
  MediumProvider mediumProvider() {
    List<Medium> media = Medium.MEDIA
      .stream()
      .map(mediumName -> mediumJpaRepository.findByName(mediumName)
        .orElseGet(() -> mediumJpaRepository.save(new MediumEntity(null, mediumName))))
      .map(mediumEntity -> new Medium(mediumEntity.id, mediumEntity.name))
      .collect(toList());

    return new MediumAccess(media);
  }

  @Bean
  QuantityEntityMapper quantityEntityMapper(QuantityProvider quantityProvider) {
    return new QuantityEntityMapper(quantityProvider);
  }

  @Bean
  DisplayQuantityEntityMapper displayQuantityEntityMapper(
    QuantityEntityMapper quantityEntityMapper
  ) {
    return new DisplayQuantityEntityMapper(quantityEntityMapper);
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

  @Bean
  LogicalMeterEntityMapper logicalMeterEntityMapper(
    MeterDefinitionEntityMapper meterDefinitionEntityMapper,
    MediumProvider mediumProvider,
    SystemMeterDefinitionProvider meterDefinitionProvider
  ) {
    return new LogicalMeterEntityMapper(
      meterDefinitionEntityMapper,
      meterDefinitionProvider,
      mediumProvider
    );
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
