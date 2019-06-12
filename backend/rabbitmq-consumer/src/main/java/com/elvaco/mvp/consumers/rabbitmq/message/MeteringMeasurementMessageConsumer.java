package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.ZonedDateTime;
import java.util.Optional;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeasurementMessageResponseBuilder;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import com.elvaco.mvp.core.access.MediumProvider;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.Organisation;
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
import static com.elvaco.mvp.core.domainmodels.PeriodRange.from;
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

    State logicalMeterState = new State();
    LogicalMeter logicalMeter = logicalMeterUseCases.findBy(organisation.id, facilityId)
      .orElseGet(() ->
        logicalMeterState.setModified(
          LogicalMeter.builder()
            .externalId(facilityId)
            .organisationId(organisation.id)
            .meterDefinition(meterDefinitionUseCases.getAutoApplied(
              organisation,
              mediumProvider.getByNameOrThrow(resolveMedium(measurementMessage.values))
            ))
            .build())
      );

    Optional<Gateway> gateway = measurementMessage.gateway()
      .map(gatewayIdDto -> gatewayIdDto.id)
      .map(serial -> gatewayUseCases.findBy(organisation.id, serial)
        .orElseGet(() -> gatewayUseCases.save(
          Gateway.builder()
            .organisationId(organisation.id)
            .serial(serial)
            .productModel("")
            .build()
          )
        ));

    if (logicalMeterState.modified) {
      gateway.ifPresentOrElse(
        gw -> logicalMeterUseCases.save(logicalMeter.toBuilder().gateway(gw).build()),
        () -> logicalMeterUseCases.save(logicalMeter)
      );
    }

    String address = measurementMessage.meter.id;
    ZonedDateTime zonedDateTime = getEarliestTimestamp(measurementMessage);

    State physicalMeterState = new State();
    PhysicalMeter physicalMeter =
      physicalMeterUseCases.findBy(organisation.id, facilityId, address)
        .orElseGet(() ->
          physicalMeterState.setModified(PhysicalMeter.builder()
            .organisationId(organisation.id)
            .address(address)
            .externalId(facilityId)
            .medium(Medium.UNKNOWN_MEDIUM)
            .logicalMeterId(logicalMeter.id)
            .readIntervalMinutes(DEFAULT_READ_INTERVAL_MINUTES)
            .activePeriod(from(zonedDateTime))
            .build())
        );

    updateActivePeriods(physicalMeter, physicalMeterState, zonedDateTime);

    if (physicalMeterState.modified) {
      physicalMeterUseCases.save(physicalMeter);
    }

    ZonedDateTime now = ZonedDateTime.now();
    measurementMessage.values
      .forEach(value -> createMeasurement(value, now, physicalMeter)
        .ifPresent(measurement -> measurementUseCases.createOrUpdate(measurement, logicalMeter)));

    MeasurementMessageResponseBuilder responseBuilder =
      new MeasurementMessageResponseBuilder(measurementMessage.organisationId);

    if (physicalMeterValidator().isIncomplete(physicalMeter)
      || logicalMeterValidator().isIncomplete(logicalMeter)) {
      responseBuilder
        .setFacilityId(facilityId)
        .setMeterExternalId(address);
    }

    gateway
      .filter(gw -> gatewayValidator().isIncomplete(gw))
      .map(gw -> responseBuilder
        .setFacilityId(facilityId)
        .setGatewayExternalId(gw.serial));

    return responseBuilder.build();
  }

  ZonedDateTime getEarliestTimestamp(MeteringMeasurementMessageDto measurementMessage)
    throws IllegalArgumentException {

    return measurementMessage.values.stream()
      .min(comparing((ValueDto v) -> v.timestamp))
      .map(dto -> dto.timestamp.atZone(METERING_TIMEZONE))
      .orElseThrow(() -> new IllegalArgumentException(
        "MeteringMeasurementMessage without timestamp " + measurementMessage));
  }

  private void updateActivePeriods(
    PhysicalMeter physicalMeter,
    State state,
    ZonedDateTime zonedDateTime
  ) {
    if (physicalMeter.activePeriod.isEmpty()) {
      physicalMeter.activePeriod = from(zonedDateTime);
      state.setModified(physicalMeter);
    }

    Optional<PhysicalMeter> activeAtTimestamp = physicalMeterUseCases.getActiveMeterAtTimestamp(
      physicalMeter.organisationId,
      physicalMeter.externalId,
      zonedDateTime
    );

    Optional<ZonedDateTime> oldStopForActiveAtTimestamp =
      activeAtTimestamp.flatMap(at -> at.activePeriod.getStopDateTime());

    // Close active at timestamp if it's another meter than the incoming measurements
    // and is not moving stop time forward
    activeAtTimestamp
      .filter(at -> !at.id.equals(physicalMeter.id))
      .filter(at -> physicalMeter.activePeriod.getStopDateTime()
        .filter(pStop -> zonedDateTime.isAfter(pStop))
        .isEmpty())
      .map(at -> physicalMeterUseCases.saveAndFlush(at.deactivate(zonedDateTime)));

    // Move start time back in time if measurement is before current start time
    boolean movedStartTime = physicalMeter.activePeriod.getStartDateTime()
      .filter(pStart -> zonedDateTime.isBefore(pStart))
      .map(pStart -> state.setModified(physicalMeter.activate(zonedDateTime)))
      .isPresent();

    // Also set stop time if this is a new meter in between two existing
    if (!movedStartTime) {
      activeAtTimestamp
        .filter(at -> !at.id.equals(physicalMeter.id))
        .flatMap(at -> oldStopForActiveAtTimestamp)
        .map(atStop -> state.setModified(physicalMeter.deactivate(atStop)));
    }
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

  private static final class State {
    private boolean modified = false;

    private <T> T setModified(T object) {
      modified = true;
      return object;
    }
  }
}
