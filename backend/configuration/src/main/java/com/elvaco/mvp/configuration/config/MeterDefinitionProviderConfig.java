package com.elvaco.mvp.configuration.config;

import java.util.List;

import com.elvaco.mvp.configuration.bootstrap.production.ProductionDataProvider;
import com.elvaco.mvp.core.access.MediumAccess;
import com.elvaco.mvp.core.access.MediumProvider;
import com.elvaco.mvp.core.access.SystemMeterDefinitionAccess;
import com.elvaco.mvp.core.access.SystemMeterDefinitionProvider;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.database.entity.meter.MediumEntity;
import com.elvaco.mvp.database.repository.jpa.MediumJpaRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.stream.Collectors.toList;

@Configuration
class MeterDefinitionProviderConfig {

  @Bean
  SystemMeterDefinitionProvider meterDefinitionProvider(
    MeterDefinitions meterDefinitions,
    ProductionDataProvider productionDataProvider
  ) {
    List<MeterDefinition> providedDefinitions =
      productionDataProvider.meterDefinitions()
        .stream()
        .map(meterDefinition ->
          meterDefinitions.findSystemMeterDefinition(meterDefinition.medium)
            .orElseGet(() -> meterDefinitions.save(meterDefinition)))
        .collect(toList());

    return new SystemMeterDefinitionAccess(providedDefinitions);
  }

  @Bean
  MediumProvider mediumProvider(MediumJpaRepository mediumJpaRepository) {
    List<Medium> media = Medium.MEDIA
      .stream()
      .map(mediumName -> mediumJpaRepository.findByName(mediumName)
        .orElseGet(() -> mediumJpaRepository.save(new MediumEntity(null, mediumName))))
      .map(mediumEntity -> new Medium(mediumEntity.id, mediumEntity.name))
      .collect(toList());

    return new MediumAccess(media);
  }
}
