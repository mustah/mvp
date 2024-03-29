package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.function.Consumer;

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
import static com.elvaco.mvp.core.domainmodels.PeriodRange.empty;
import static com.elvaco.mvp.core.domainmodels.PeriodRange.from;
import static com.elvaco.mvp.core.util.CompletenessValidators.GATEWAY_VALIDATOR;
import static com.elvaco.mvp.core.util.CompletenessValidators.LOGICAL_METER_VALIDATOR;
import static com.elvaco.mvp.core.util.CompletenessValidators.PHYSICAL_METER_VALIDATOR;
import static java.util.Comparator.comparing;

@Slf4j
@RequiredArgsConstructor
public class MeteringMeasurementMessageConsumer implements MeasurementMessageConsumer {

  private static final Consumer<PhysicalMeter> DO_NOTHING = pm -> { };

  private static final LocalDateTime FIRST_VALID_DATE = LocalDateTime.parse("2001-01-01T00:00:00");

  private final LogicalMeterUseCases logicalMeterUseCases;
  private final PhysicalMeterUseCases physicalMeterUseCases;
  private final OrganisationUseCases organisationUseCases;
  private final MeasurementUseCases measurementUseCases;
  private final GatewayUseCases gatewayUseCases;
  private final MeterDefinitionUseCases meterDefinitionUseCases;
  private final UnitConverter unitConverter;
  private final MediumProvider mediumProvider;

  @Override
  public Optional<GetReferenceInfoDto> accept(MeteringMeasurementMessageDto messageDto) {
    String facilityId = messageDto.facility.id;

    if (facilityId.trim().isEmpty()) {
      log.warn(
        "Discarding measurement message with invalid facility/external ID: {}",
        messageDto
      );
      return Optional.empty();
    }

    Organisation organisation = organisationUseCases.findOrCreate(messageDto.organisationId);

    State logicalMeterState = new State();
    LogicalMeter logicalMeter = logicalMeterUseCases.findBy(organisation.id, facilityId)
      .orElseGet(() -> logicalMeterState.setModified(
        LogicalMeter.builder()
          .externalId(facilityId)
          .organisationId(organisation.id)
          .meterDefinition(meterDefinitionUseCases.getAutoApplied(
            organisation,
            mediumProvider.getByNameOrThrow(resolveMedium(messageDto.values))
          ))
          .build()
      ));

    Optional<Gateway> gateway = messageDto.gateway()
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

    ZonedDateTime zonedDateTime = getEarliestTimestamp(messageDto);

    if (logicalMeterState.modified.isPresent()) {
      gateway.ifPresentOrElse(
        gw -> logicalMeterUseCases.save(
          logicalMeter.toBuilder().gateway(
            gw.toBuilder()
              .created(zonedDateTime)
              .lastSeen(zonedDateTime)
              .build())
            .build()),
        () -> logicalMeterUseCases.save(logicalMeter)
      );
    } else {
      gateway.ifPresent(gw -> gatewayUseCases.setLastSeenForMeter(gw, logicalMeter, zonedDateTime));
    }

    String address = messageDto.meter.id;
    boolean isValidDate = zonedDateTime.isAfter(FIRST_VALID_DATE.atZone(METERING_TIMEZONE));

    State physicalMeterState = new State();
    PhysicalMeter physicalMeter =
      physicalMeterUseCases.findBy(organisation.id, facilityId, address)
        .orElseGet(() -> physicalMeterState.setModified(
          PhysicalMeter.builder()
            .organisationId(organisation.id)
            .address(address)
            .externalId(facilityId)
            .medium(Medium.UNKNOWN_MEDIUM)
            .logicalMeterId(logicalMeter.id)
            .readIntervalMinutes(DEFAULT_READ_INTERVAL_MINUTES)
            .activePeriod(isValidDate ? from(zonedDateTime) : empty())
            .build())
        );

    if (isValidDate) {
      updateActivePeriods(physicalMeter, physicalMeterState, zonedDateTime);
    }

    physicalMeterState.modified.ifPresent(o -> physicalMeterUseCases.save(physicalMeter));

    ZonedDateTime now = ZonedDateTime.now();
    messageDto.values.forEach(value -> createMeasurement(value, now, physicalMeter)
      .ifPresent(measurement -> measurementUseCases.createOrUpdate(measurement, logicalMeter)));

    MeasurementMessageResponseBuilder responseBuilder =
      new MeasurementMessageResponseBuilder(messageDto.organisationId);

    if (PHYSICAL_METER_VALIDATOR.isIncomplete(physicalMeter)
      || LOGICAL_METER_VALIDATOR.isIncomplete(logicalMeter)) {
      responseBuilder.facilityId(facilityId).meterExternalId(address);
    }

    gateway.filter(GATEWAY_VALIDATOR::isIncomplete)
      .map(gw -> responseBuilder.facilityId(facilityId).gatewayExternalId(gw.serial));

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
    State physicalMeterState,
    ZonedDateTime earliestDateTime
  ) {
    if (physicalMeter.activePeriod.isEmpty()) {
      physicalMeter.activePeriod = from(earliestDateTime);
      physicalMeterState.setModified(physicalMeter);
    }

    Optional<PhysicalMeter> physicalMeterActiveAtTimestamp =
      physicalMeterUseCases.getActiveMeterAtTimestamp(
        physicalMeter.organisationId,
        physicalMeter.externalId,
        earliestDateTime
      );

    Optional<ZonedDateTime> oldStopTimeDateForActiveMeter =
      physicalMeterActiveAtTimestamp.flatMap(pm -> pm.activePeriod.getStopDateTime());

    // Close active at timestamp if it's another meter than the incoming measurements
    // and is not moving stop time forward
    physicalMeterActiveAtTimestamp
      .filter(pm -> !pm.id.equals(physicalMeter.id))
      .filter(pm -> physicalMeter.activePeriod.getStopDateTime()
        .filter(earliestDateTime::isAfter)
        .isEmpty())
      .map(pm -> pm.deactivate(earliestDateTime))
      .ifPresent(physicalMeterUseCases::saveAndFlush);

    // Move start time back in time if measurement is before current start time
    physicalMeter.activePeriod.getStartDateTime()
      .filter(earliestDateTime::isBefore)
      .map(physicalMeter::activate)
      .map(pm -> physicalMeter.activate(earliestDateTime))
      .map(physicalMeterState::setModified)
      .ifPresentOrElse(
        DO_NOTHING,
        () -> {
          // Also set stop time if this is a new meter in between two existing
          physicalMeterActiveAtTimestamp
            .filter(at -> !at.id.equals(physicalMeter.id))
            .flatMap(at -> oldStopTimeDateForActiveMeter)
            .map(physicalMeter::deactivate)
            .ifPresent(physicalMeterState::setModified);
        }
      );
  }

  private Optional<Measurement> createMeasurement(
    ValueDto value,
    ZonedDateTime receivedTime,
    PhysicalMeter physicalMeter
  ) {
    Optional<Quantity> quantity = mappedQuantity(value.quantity);
    if (quantity.isEmpty()) {
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
    if (value.timestamp.isBefore(FIRST_VALID_DATE)) {
      log.warn(
        "Discarding measurement with invalid date for facility '{}' and meter '{}', got {}",
        physicalMeter.externalId,
        physicalMeter.address,
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
    private Optional modified = Optional.empty();

    private <T> T setModified(T object) {
      modified = Optional.of(object);
      return object;
    }
  }
}
