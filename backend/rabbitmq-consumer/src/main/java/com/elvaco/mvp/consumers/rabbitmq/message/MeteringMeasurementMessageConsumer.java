package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.ZoneId;
import java.util.Optional;
import java.util.function.Supplier;

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
import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.mappedQuantityName;
import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.resolveMeterDefinition;
import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static com.elvaco.mvp.core.domainmodels.Medium.UNKNOWN_MEDIUM;
import static com.elvaco.mvp.core.util.CompletenessValidators.gatewayValidator;
import static com.elvaco.mvp.core.util.CompletenessValidators.logicalMeterValidator;
import static com.elvaco.mvp.core.util.CompletenessValidators.physicalMeterValidator;
import static java.util.UUID.randomUUID;

@Slf4j
@RequiredArgsConstructor
public class MeteringMeasurementMessageConsumer implements MeasurementMessageConsumer {

  /**
   * Metering stores and treats all values as CET.
   * At least it's consistent!
   */
  public static final ZoneId METERING_TIMEZONE = ZoneId.of("CET");

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

    AlreadyCreated existing = new AlreadyCreated();

    LogicalMeter logicalMeter = logicalMeterUseCases.findBy(organisation.id, facilityId)
      .map(existing::logicalMeter)
      .orElseGet(() -> {
        Medium medium = Medium.from(resolveMeterDefinition(measurementMessage.values).medium);
        return LogicalMeter.builder()
          .id(randomUUID())
          .externalId(facilityId)
          .organisationId(organisation.id)
          .meterDefinition(MeterDefinition.fromMedium(medium))
          .location(UNKNOWN_LOCATION)
          .build();
      });

    String address = measurementMessage.meter.id;

    PhysicalMeter physicalMeter =
      physicalMeterUseCases.findBy(organisation.id, facilityId, address)
        .map(existing::physicalMeter)
        .orElseGet(() -> PhysicalMeter.builder()
          .organisation(organisation)
          .address(address)
          .externalId(facilityId)
          .medium(UNKNOWN_MEDIUM.medium)
          .logicalMeterId(logicalMeter.id)
          .readIntervalMinutes(0)
          .build()
        );

    LogicalMeter connectedLogicalMeter = measurementMessage.gateway()
      .map(gatewayIdDto -> gatewayIdDto.id)
      .map(serial -> gatewayUseCases.findBy(organisation.id, serial)
        .orElseGet(() -> gatewayUseCases.save(
          Gateway.builder()
            .organisationId(organisation.id)
            .serial(serial)
            .productModel("")
            .meter(logicalMeter)
            .build()
        )))
      .map(gateway -> {
        if (gatewayValidator().isIncomplete(gateway)) {
          responseBuilder.setGatewayExternalId(gateway.serial);
          responseBuilder.setFacilityId(facilityId);
        }
        return logicalMeter.withGateway(gateway).withPhysicalMeter(physicalMeter);
      })
      .orElseGet(() -> logicalMeter.withPhysicalMeter(physicalMeter));

    existing.shouldSaveLogicalMeter(() -> logicalMeterUseCases.save(connectedLogicalMeter));
    existing.shouldSavePhysicalMeter(() -> physicalMeterUseCases.save(physicalMeter));

    measurementMessage.values.stream()
      .map(value -> createMeasurement(value, physicalMeter))
      .forEach(measurementUseCases::save);

    if (physicalMeterValidator().isIncomplete(physicalMeter)
      || logicalMeterValidator().isIncomplete(connectedLogicalMeter)) {
      responseBuilder.setFacilityId(facilityId);
      responseBuilder.setMeterExternalId(address);
    }

    return responseBuilder.build();
  }

  private Measurement createMeasurement(ValueDto value, PhysicalMeter physicalMeter) {
    return Measurement.builder()
      .physicalMeter(physicalMeter)
      .created(value.timestamp.atZone(METERING_TIMEZONE))
      .value(value.value)
      .unit(value.unit)
      .quantity(mappedQuantityName(value.quantity))
      .build();
  }

  private static final class AlreadyCreated {

    private LogicalMeter logicalMeter;
    private PhysicalMeter physicalMeter;

    private LogicalMeter logicalMeter(LogicalMeter logicalMeter) {
      this.logicalMeter = logicalMeter;
      return logicalMeter;
    }

    private PhysicalMeter physicalMeter(PhysicalMeter physicalMeter) {
      this.physicalMeter = physicalMeter;
      return physicalMeter;
    }

    private void shouldSaveLogicalMeter(Supplier<LogicalMeter> supplier) {
      if (logicalMeter == null || logicalMeter.gateways.isEmpty()) {
        supplier.get();
      }
    }

    private void shouldSavePhysicalMeter(Supplier<PhysicalMeter> supplier) {
      if (physicalMeter == null) {
        supplier.get();
      }
    }
  }
}
