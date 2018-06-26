package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeasurementMessageResponseBuilder;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;
import com.elvaco.mvp.producers.rabbitmq.dto.GatewayIdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageHelper.removeSimultaneousQuantityValues;
import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.mappedQuantityName;
import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.resolveMeterDefinition;
import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static com.elvaco.mvp.core.domainmodels.Medium.UNKNOWN_MEDIUM;
import static com.elvaco.mvp.core.util.CompletenessValidators.gatewayValidator;
import static com.elvaco.mvp.core.util.CompletenessValidators.logicalMeterValidator;
import static com.elvaco.mvp.core.util.CompletenessValidators.physicalMeterValidator;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
public class MeteringMeasurementMessageConsumer implements MeasurementMessageConsumer {

  /**
   * Metering stores and treats all values as CET.
   * At least it's consistent!
   */
  static final ZoneId METERING_TIMEZONE = ZoneId.of("CET");

  private final LogicalMeterUseCases logicalMeterUseCases;
  private final PhysicalMeterUseCases physicalMeterUseCases;
  private final OrganisationUseCases organisationUseCases;
  private final MeasurementUseCases measurementUseCases;
  private final GatewayUseCases gatewayUseCases;

  @Override
  public Optional<GetReferenceInfoDto> accept(MeteringMeasurementMessageDto measurementMessage) {
    String facilityId = measurementMessage.facility.id;
    if (facilityId.trim().isEmpty()) {
      log.warn(
        "Discarding measurement message with invalid facility/external ID: {}",
        measurementMessage
      );
      return Optional.empty();
    }

    Organisation organisation =
      organisationUseCases.findOrCreate(measurementMessage.organisationId);

    MeasurementMessageResponseBuilder responseBuilder =
      new MeasurementMessageResponseBuilder(measurementMessage.organisationId);

    LogicalMeter logicalMeter =
      logicalMeterUseCases.findByOrganisationIdAndExternalId(
        organisation.id,
        facilityId
      ).orElseGet(() -> {
        Medium medium = Medium.from(resolveMeterDefinition(measurementMessage.values).medium);
        return new LogicalMeter(
          randomUUID(),
          facilityId,
          organisation.id,
          MeterDefinition.fromMedium(medium),
          UNKNOWN_LOCATION
        );
      });

    PhysicalMeter physicalMeter = physicalMeterUseCases
      .findByOrganisationIdAndExternalIdAndAddress(
        organisation.id,
        facilityId,
        measurementMessage.meter.id
      ).orElseGet(() -> PhysicalMeter.builder()
        .organisation(organisation)
        .address(measurementMessage.meter.id)
        .externalId(facilityId)
        .medium(UNKNOWN_MEDIUM.medium)
        .logicalMeterId(logicalMeter.id)
        .readIntervalMinutes(0)
        .build());

    if (physicalMeterValidator().isIncomplete(physicalMeter)
      || logicalMeterValidator().isIncomplete(logicalMeter)) {
      responseBuilder.setFacilityId(facilityId);
      responseBuilder.setMeterExternalId(measurementMessage.meter.id);
    }

    Gateway gateway = null;
    if (measurementMessage.gateway().isPresent()) {
      GatewayIdDto gatewayId = measurementMessage.gateway().get();
      gateway = gatewayUseCases.findBy(
        organisation.id,
        gatewayId.id
      ).orElseGet(() ->
        Gateway.builder()
          .organisationId(organisation.id)
          .serial(gatewayId.id)
          .productModel("")
          .meter(logicalMeter)
          .build()
      );

      if (gatewayValidator().isIncomplete(gateway)) {
        responseBuilder.setFacilityId(facilityId);
        responseBuilder.setGatewayExternalId(gatewayId.id);
      }
    }

    List<Measurement> measurements =
      removeSimultaneousQuantityValues(measurementMessage.values).stream()
        .map(value -> findOrCreateMeasurement(value, physicalMeter))
        .collect(toList());

    if (gateway != null) {
      gatewayUseCases.save(gateway);
      logicalMeterUseCases.save(
        logicalMeter
          .withGateway(gateway)
          .withPhysicalMeter(physicalMeter));
    } else {
      logicalMeterUseCases.save(logicalMeter.withPhysicalMeter(physicalMeter));
    }

    physicalMeterUseCases.save(physicalMeter);
    measurementUseCases.save(measurements);

    return responseBuilder.build();
  }

  private Measurement findOrCreateMeasurement(ValueDto value, PhysicalMeter physicalMeter) {
    return measurementUseCases.findBy(
      physicalMeter.id,
      mappedQuantityName(value.quantity),
      value.timestamp.atZone(METERING_TIMEZONE)
    ).orElseGet(() ->
      Measurement.builder()
        .physicalMeter(physicalMeter)
        .created(value.timestamp.atZone(METERING_TIMEZONE))
        .quantity(mappedQuantityName(value.quantity))
        .value(value.value)
        .unit(value.unit)
        .build()
    ).withValue(value.value)
      .withUnit(value.unit)
      .withQuantity(mappedQuantityName(value.quantity));
  }
}
