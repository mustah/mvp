package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.consumers.rabbitmq.message.AlarmMessageConsumer;
import com.elvaco.mvp.consumers.rabbitmq.message.InfrastructureMessageConsumer;
import com.elvaco.mvp.consumers.rabbitmq.message.InfrastructureStatusMessageConsumer;
import com.elvaco.mvp.consumers.rabbitmq.message.MeasurementMessageConsumer;
import com.elvaco.mvp.consumers.rabbitmq.message.MessageListener;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringAlarmMessageConsumer;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMeasurementMessageConsumer;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageListener;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageParser;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringReferenceInfoMessageConsumer;
import com.elvaco.mvp.consumers.rabbitmq.message.ReferenceInfoMessageConsumer;
import com.elvaco.mvp.core.access.MediumProvider;
import com.elvaco.mvp.core.spi.amqp.JobService;
import com.elvaco.mvp.core.spi.geocode.GeocodeService;
import com.elvaco.mvp.core.spi.repository.MeterAlarmLogs;
import com.elvaco.mvp.core.unitconverter.UnitConverter;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.core.usecase.MeterDefinitionUseCases;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;
import com.elvaco.mvp.core.usecase.PropertiesUseCases;
import com.elvaco.mvp.core.util.MessageThrottler;
import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringReferenceInfoMessageDto;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MeteringMessageListenerConfig {
  private final LogicalMeterUseCases logicalMeterUseCases;
  private final PhysicalMeterUseCases physicalMeterUseCases;
  private final OrganisationUseCases organisationUseCases;
  private final MeasurementUseCases measurementUseCases;
  private final GatewayUseCases gatewayUseCases;
  private final MeterDefinitionUseCases meterDefinitionUseCases;
  private final GeocodeService geocodeService;
  private final PropertiesUseCases propertiesUseCases;
  private final MeterAlarmLogs meterAlarmLogs;
  private final UnitConverter unitConverter;
  private final MediumProvider mediumProvider;

  @Bean
  MeasurementMessageConsumer measurementMessageConsumer() {
    return new MeteringMeasurementMessageConsumer(
      logicalMeterUseCases,
      physicalMeterUseCases,
      organisationUseCases,
      measurementUseCases,
      gatewayUseCases,
      meterDefinitionUseCases,
      unitConverter,
      mediumProvider
    );
  }

  @Bean
  ReferenceInfoMessageConsumer referenceInfoMessageConsumer(
    JobService<MeteringReferenceInfoMessageDto> meterSyncJobService
  ) {
    return new MeteringReferenceInfoMessageConsumer(
      logicalMeterUseCases,
      physicalMeterUseCases,
      organisationUseCases,
      gatewayUseCases,
      geocodeService,
      propertiesUseCases,
      meterSyncJobService,
      mediumProvider,
      meterDefinitionUseCases
    );
  }

  @Bean
  AlarmMessageConsumer alarmMessageConsumer() {
    return new MeteringAlarmMessageConsumer(
      physicalMeterUseCases,
      organisationUseCases,
      meterAlarmLogs
    );
  }

  @Bean
  InfrastructureMessageConsumer infrastructureStatusMessageConsumer() {
    return new InfrastructureStatusMessageConsumer(gatewayUseCases);
  }

  @Bean
  MessageListener messageListener(
    MeasurementMessageConsumer measurementMessageConsumer,
    ReferenceInfoMessageConsumer referenceInfoMessageConsumer,
    AlarmMessageConsumer alarmMessageConsumer,
    InfrastructureMessageConsumer infrastructureStatusMessageConsumer,
    MessageThrottler<String, GetReferenceInfoDto> meteringMessageThrottler
  ) {
    return new MeteringMessageListener(
      new MeteringMessageParser(),
      measurementMessageConsumer,
      referenceInfoMessageConsumer,
      alarmMessageConsumer,
      infrastructureStatusMessageConsumer,
      meteringMessageThrottler
    );
  }
}
