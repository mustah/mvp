package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.function.Supplier;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeasurementMessageResponseBuilder;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import com.elvaco.mvp.core.access.MediumProvider;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PeriodBound;
import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.unitconverter.UnitConverter;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.core.usecase.MeterDefinitionUseCases;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;
import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.DEFAULT_READ_INTERVAL_MINUTES;
import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.METERING_TIMEZONE;
import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.mappedQuantity;
import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.resolveMedium;
import static com.elvaco.mvp.core.util.CompletenessValidators.gatewayValidator;
import static com.elvaco.mvp.core.util.CompletenessValidators.logicalMeterValidator;
import static com.elvaco.mvp.core.util.CompletenessValidators.physicalMeterValidator;
import static java.util.Comparator.comparing;

@Slf4j
@RequiredArgsConstructor
public class MeteringMeasurementMessageConsumer implements MeasurementMessageConsumer {

  private final LogicalMeterUseCases logicalMeterUseCases;
  private final PhysicalMeterUseCases physicalMeterUseCases;
  private final OrganisationUseCases organisationUseCases;
  private final MeasurementUseCases measurementUseCases;
  private final GatewayUseCases gatewayUseCases;
  private final MeterDefinitionUseCases meterDefinitionUseCases;
  private final UnitConverter unitConverter;
  private final MediumProvider mediumProvider;

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
      .map(existing::setLogicalMeter)
      .orElseGet(() -> LogicalMeter.builder()
        .externalId(facilityId)
        .organisationId(organisation.id)
        .meterDefinition(meterDefinitionUseCases.getAutoApplied(
          organisation,
          mediumProvider.getByNameOrThrow(resolveMedium(measurementMessage.values))
        ))
        .build());

    String address = measurementMessage.meter.id;
    ZonedDateTime zonedMeasurementTimestamp = getEarliestTimestamp(measurementMessage);

    PhysicalMeter physicalMeter =
      physicalMeterUseCases.findBy(organisation.id, facilityId, address)
        .map(existing::setPhysicalMeter)
        .orElseGet(() -> PhysicalMeter.builder()
          .organisationId(organisation.id)
          .address(address)
          .externalId(facilityId)
          .medium(Medium.UNKNOWN_MEDIUM)
          .logicalMeterId(logicalMeter.id)
          .readIntervalMinutes(DEFAULT_READ_INTERVAL_MINUTES)
          .activePeriod(PeriodRange.halfOpenFrom(zonedMeasurementTimestamp, null))
          .build()
        );

    LogicalMeter connectedLogicalMeter = measurementMessage.gateway()
      .map(gatewayIdDto -> gatewayIdDto.id)
      .map(serial -> gatewayUseCases.findBy(organisation.id, serial)
        .orElseGet(() -> gatewayUseCases.save(Gateway.builder()
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
        return logicalMeter.toBuilder()
          .gateway(gateway).build();
      })
      .orElse(logicalMeter);

    // Update start time for measurement before current start time
    physicalMeter.activePeriod.getStartDateTime()
      .filter(zonedMeasurementTimestamp::isBefore)
      .ifPresent(start -> {
        physicalMeterUseCases.getActiveMeterAtTimestamp(
          physicalMeter.organisationId,
          physicalMeter.externalId,
          zonedMeasurementTimestamp
        ).ifPresent(otherActive -> {
          otherActive.activePeriod = otherActive.activePeriod.toBuilder()
            .stop(PeriodBound.exclusiveOf(zonedMeasurementTimestamp))
            .build();
          physicalMeterUseCases.save(otherActive);
        });

        physicalMeter.activePeriod = physicalMeter.activePeriod.toBuilder()
          .start(PeriodBound.inclusiveOf(zonedMeasurementTimestamp))
          .build();
        existing.setPhysicalMeterUpdated(physicalMeter);
      });

    existing.shouldSaveLogicalMeter(() -> logicalMeterUseCases.save(connectedLogicalMeter));
    existing.shouldSavePhysicalMeter(() -> {
      if (physicalMeter.activePeriod.isEmpty()) {
        physicalMeter.activePeriod = PeriodRange.from(PeriodBound.inclusiveOf(
          zonedMeasurementTimestamp));
      }

      // Set stop time if this is a new meter in between two existing
      physicalMeterUseCases.getActiveMeterAtTimestamp(
        physicalMeter.organisationId,
        physicalMeter.externalId,
        zonedMeasurementTimestamp
      ).filter(p -> !p.isActive(ZonedDateTime.now()))
        .map(p -> p.activePeriod.stop)
        .ifPresent(stop ->
          physicalMeter.activePeriod = physicalMeter.activePeriod.toBuilder()
            .stop(stop)
            .build());

      physicalMeterUseCases.deactivatePreviousPhysicalMeter(
        physicalMeter,
        zonedMeasurementTimestamp
      );
      return physicalMeterUseCases.save(physicalMeter);
    });

    ZonedDateTime now = ZonedDateTime.now();
    measurementMessage.values
      .forEach(value -> createMeasurement(value, now, physicalMeter)
        .ifPresent(
          (measurement) -> measurementUseCases.createOrUpdate(measurement, connectedLogicalMeter)
        ));

    if (physicalMeterValidator().isIncomplete(physicalMeter)
      || logicalMeterValidator().isIncomplete(connectedLogicalMeter)) {
      responseBuilder.setFacilityId(facilityId);
      responseBuilder.setMeterExternalId(address);
    }

    return responseBuilder.build();
  }

  protected ZonedDateTime getEarliestTimestamp(MeteringMeasurementMessageDto measurementMessage)
    throws IllegalArgumentException {

    return measurementMessage.values.stream()
      .min(comparing((ValueDto v) -> v.timestamp))
      .map(dto -> dto.timestamp.atZone(METERING_TIMEZONE))
      .orElseThrow(() -> new IllegalArgumentException(
        "MeteringMeasurementMessage without timestamp " + measurementMessage));
  }

  private Optional<Measurement> createMeasurement(
    ValueDto value,
    ZonedDateTime receivedTime,
    PhysicalMeter physicalMeter
  ) {
    Optional<Quantity> quantity = mappedQuantity(value.quantity);
    if (!quantity.isPresent()) {
      log.warn(
        "Discarding measurement with unknown quantity for facility '{}': {}",
        physicalMeter.externalId,
        value
      );
      return Optional.empty();
    }
    if (!unitConverter.isSameDimension(quantity.get().storageUnit, value.unit)) {
      log.warn(
        "Discarding measurement with invalid unit for facility '{}', expecting '{}', got {}",
        physicalMeter.externalId,
        quantity.get().storageUnit,
        value
      );
      return Optional.empty();
    }

    if (!physicalMeter.activePeriod.contains(value.timestamp.atZone(METERING_TIMEZONE))) {
      log.warn(
        "Received measurement '{}' outside active period for physical meter '{}'",
        value,
        physicalMeter
      );
    }

    return Optional.of(Measurement.builder()
      .physicalMeter(physicalMeter)
      .readoutTime(value.timestamp.atZone(METERING_TIMEZONE))
      .receivedTime(receivedTime)
      .value(value.value)
      .unit(value.unit)
      .quantity(quantity.get().name)
      .build());
  }

  private static final class AlreadyCreated {

    private LogicalMeter logicalMeter;
    private PhysicalMeter physicalMeter;
    private boolean physicalMeterUpdate = false;

    private LogicalMeter setLogicalMeter(LogicalMeter logicalMeter) {
      this.logicalMeter = logicalMeter;
      return logicalMeter;
    }

    private PhysicalMeter setPhysicalMeter(PhysicalMeter physicalMeter) {
      this.physicalMeter = physicalMeter;
      return physicalMeter;
    }

    private PhysicalMeter setPhysicalMeterUpdated(PhysicalMeter physicalMeter) {
      this.physicalMeter = physicalMeter;
      this.physicalMeterUpdate = true;
      return physicalMeter;
    }

    private void shouldSaveLogicalMeter(Supplier<LogicalMeter> supplier) {
      if (logicalMeter == null || logicalMeter.gateways.isEmpty()) {
        supplier.get();
      }
    }

    private void shouldSavePhysicalMeter(Supplier<PhysicalMeter> supplier) {
      if (physicalMeter == null || physicalMeterUpdate || physicalMeter.activePeriod.isEmpty()) {
        supplier.get();
      }
    }
  }
}
