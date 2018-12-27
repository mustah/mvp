package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.function.Supplier;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeasurementMessageResponseBuilder;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PeriodBound;
import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.unitconverter.UnitConverter;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;
import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.METERING_TIMEZONE;
import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.mappedQuantity;
import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.resolveMeterDefinition;
import static com.elvaco.mvp.core.domainmodels.Medium.UNKNOWN_MEDIUM;
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
  private final UnitConverter unitConverter;

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
        .meterDefinition(resolveMeterDefinition(measurementMessage.values))
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
          .medium(UNKNOWN_MEDIUM.medium)
          .logicalMeterId(logicalMeter.id)
          .readIntervalMinutes(0)
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
          .gateway(gateway).build()
          .addPhysicalMeter(physicalMeter);
      })
      .orElseGet(() -> logicalMeter.addPhysicalMeter(physicalMeter));

    existing.shouldSaveLogicalMeter(() -> logicalMeterUseCases.save(connectedLogicalMeter));
    existing.shouldSavePhysicalMeter(() -> {
      if (physicalMeter.activePeriod.isEmpty()) {
        physicalMeter.activePeriod = PeriodRange.from(PeriodBound.inclusiveOf(
          zonedMeasurementTimestamp));
      }
      var m = physicalMeterUseCases.save(physicalMeter);
      physicalMeterUseCases.deactivatePreviousPhysicalMeter(
        physicalMeter,
        zonedMeasurementTimestamp
      );
      return m;
    });

    measurementMessage.values
      .forEach(value -> createMeasurement(
        value,
        physicalMeter
      ).ifPresent(measurementUseCases::createOrUpdate));

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
      .map(dto -> dto.timestamp)
      .orElseThrow(() -> new IllegalArgumentException(
        "MeteringMeasurementMessage without timestamp " + measurementMessage))
      .atZone(METERING_TIMEZONE);
  }

  private Optional<Measurement> createMeasurement(ValueDto value, PhysicalMeter physicalMeter) {
    Optional<Quantity> quantity = mappedQuantity(value.quantity);
    if (!quantity.isPresent()) {
      log.warn(
        "Discarding measurement with unknown quantity for facility '{}': {}",
        physicalMeter.externalId,
        value
      );
      return Optional.empty();
    }
    if (!unitConverter.isSameDimension(quantity.get().presentationUnit(), value.unit)) {
      log.warn(
        "Discarding measurement with invalid unit for facility '{}', expecting '{}', got {}",
        physicalMeter.externalId,
        quantity.get().presentationUnit(),
        value
      );
      return Optional.empty();
    }

    if (!physicalMeter.activePeriod.contains(value.timestamp.atZone(METERING_TIMEZONE))) {
      log.warn(
        "Received mesaurement '{}' outside active period for physical meter '{}'",
        value,
        physicalMeter
      );
    }

    return Optional.of(Measurement.builder()
      .physicalMeter(physicalMeter)
      .created(value.timestamp.atZone(METERING_TIMEZONE))
      .value(value.value)
      .unit(value.unit)
      .quantity(quantity.get().name)
      .build());
  }

  private static final class AlreadyCreated {

    private LogicalMeter logicalMeter;
    private PhysicalMeter physicalMeter;

    private LogicalMeter setLogicalMeter(LogicalMeter logicalMeter) {
      this.logicalMeter = logicalMeter;
      return logicalMeter;
    }

    private PhysicalMeter setPhysicalMeter(PhysicalMeter physicalMeter) {
      this.physicalMeter = physicalMeter;
      return physicalMeter;
    }

    private void shouldSaveLogicalMeter(Supplier<LogicalMeter> supplier) {
      if (logicalMeter == null || logicalMeter.gateways.isEmpty()) {
        supplier.get();
      }
    }

    private void shouldSavePhysicalMeter(Supplier<PhysicalMeter> supplier) {
      if (physicalMeter == null || physicalMeter.activePeriod.isEmpty()) {
        supplier.get();
      }
    }
  }
}
