package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.consumers.rabbitmq.dto.FacilityDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringAlarmMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeterStructureMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringResponseDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LocationWithId;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.spi.geocode.GeocodeService;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.DISTRICT_HEATING_METER_QUANTITIES;
import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.METER_TO_MVP_QUANTITIES;
import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
public class MeteringMessageHandler implements MessageHandler {

  static final ZoneId METERING_TIMEZONE = ZoneId.of("CET");

  private final LogicalMeterUseCases logicalMeterUseCases;
  private final PhysicalMeterUseCases physicalMeterUseCases;
  private final OrganisationUseCases organisationUseCases;
  private final MeasurementUseCases measurementUseCases;
  private final GatewayUseCases gatewayUseCases;
  private final GeocodeService geocodeService;

  @Override
  public Optional<MeteringResponseDto> handle(MeteringMeterStructureMessageDto structureMessage) {
    Organisation organisation = findOrCreateOrganisation(structureMessage.organisationId);
    FacilityDto facility = structureMessage.facility;

    if (facilityIdIsInvalid(facility.id)) {
      log.warn("Discarding message with invalid facility/external ID '{}'", facility.id);
      return Optional.empty();
    }

    Location location = new LocationBuilder()
      .country(facility.country)
      .city(facility.city)
      .address(facility.address)
      .build();

    LogicalMeter logicalMeter = logicalMeterUseCases
      .findByOrganisationIdAndExternalId(organisation.id, facility.id)
      .orElseGet(() ->
        new LogicalMeter(
          randomUUID(),
          facility.id,
          organisation.id,
          MeterDefinition.fromMedium(Medium.from(structureMessage.meter.medium)),
          location
        )).withLocation(location);

    PhysicalMeter physicalMeter = findOrCreatePhysicalMeter(
      facility.id,
      structureMessage.meter.id,
      structureMessage.meter.medium,
      structureMessage.meter.manufacturer,
      logicalMeter.id,
      organisation
    ).withMedium(structureMessage.meter.medium)
      .withManufacturer(structureMessage.meter.manufacturer)
      .withLogicalMeterId(logicalMeter.id)
      .withReadInterval(structureMessage.meter.expectedInterval)
      .replaceActiveStatus(StatusType.from(structureMessage.meter.status));

    Gateway gateway = findOrCreateGateway(
      organisation,
      logicalMeter,
      structureMessage.gateway.id,
      structureMessage.gateway.productModel
    ).withProductModel(structureMessage.gateway.productModel)
      .replaceActiveStatus(StatusType.from(structureMessage.gateway.status));

    gatewayUseCases.save(gateway);

    LogicalMeter meter = logicalMeter
      .withGateway(gateway)
      .withPhysicalMeter(physicalMeter);

    logicalMeterUseCases.save(meter);

    physicalMeterUseCases.save(physicalMeter);

    geocodeService.fetchCoordinates(LocationWithId.of(meter.location, meter.id));

    return Optional.empty();
  }

  @Override
  public Optional<? extends MeteringResponseDto> handle(
    MeteringMeasurementMessageDto measurementMessage
  ) {
    Organisation organisation = findOrCreateOrganisation(measurementMessage.organisationId);

    String medium = selectMeterDefinition(measurementMessage.values).medium;
    GetReferenceInfoDtoBuilder referenceInfoDtoBuilder =
      new GetReferenceInfoDtoBuilder(measurementMessage.organisationId);

    String facilityId = measurementMessage.facility.id;

    if (facilityIdIsInvalid(facilityId)) {
      log.warn("Discarding measurement message with invalid facility/external ID '{}'", facilityId);
      return Optional.empty();
    }

    LogicalMeter logicalMeter =
      logicalMeterUseCases.findByOrganisationIdAndExternalId(
        organisation.id,
        facilityId
      ).orElseGet(() -> {
          referenceInfoDtoBuilder.setMeterExternalId(facilityId);
          return new LogicalMeter(
            randomUUID(),
            facilityId,
            organisation.id,
            MeterDefinition.fromMedium(Medium.from(medium)),
            UNKNOWN_LOCATION
          );
        }
      );

    PhysicalMeter physicalMeter = physicalMeterUseCases
      .findByOrganisationIdAndExternalIdAndAddress(
        organisation.id,
        facilityId,
        measurementMessage.meter.id
      ).orElseGet(() -> {
          referenceInfoDtoBuilder.setMeterExternalId(facilityId);
          return new PhysicalMeter(
            randomUUID(),
            organisation,
            measurementMessage.meter.id,
            facilityId,
            Medium.UNKNOWN_MEDIUM.medium,
            "UNKNOWN",
            logicalMeter.id,
            0L, // TODO add real interval here!
            null
          );
        }
      );
    Optional<Gateway> optionalGateway = Optional.empty();
    if (measurementMessage.gateway != null) {
      optionalGateway = Optional.of(gatewayUseCases.findBy(
        organisation.id,
        measurementMessage.gateway.id
      ).orElseGet(() -> {
        referenceInfoDtoBuilder.setGatewayExternalId(measurementMessage.gateway.id);
        return new Gateway(
          randomUUID(),
          organisation.id,
          measurementMessage.gateway.id,
          "Unknown",
          singletonList(logicalMeter),
          emptyList() // TODO Save gateway status
        );
      }));
    }
    List<Measurement> measurements = measurementMessage.values
      .stream()
      .map(value -> measurementUseCases
        .findForMeterCreatedAt(
          physicalMeter.id,
          mappedQuantity(value.quantity).name,
          value.timestamp.atZone(METERING_TIMEZONE)
        ).orElseGet(() ->
          new Measurement(
            null,
            // Note: Metering stores and treats all values as CET - at least
            // it's consistent!
            value.timestamp.atZone(METERING_TIMEZONE),
            mappedQuantity(value.quantity).name,
            value.value,
            value.unit,
            physicalMeter
          )
        ).withValue(value.value)
        .withUnit(value.unit)
        .withQuantity(mappedQuantity(value.quantity).name)
      )
      .collect(toList());

    if (optionalGateway.isPresent()) {
      gatewayUseCases.save(optionalGateway.get());
      logicalMeterUseCases.save(
        logicalMeter
          .withGateway(optionalGateway.get())
          .withPhysicalMeter(physicalMeter));
    } else {
      logicalMeterUseCases.save(logicalMeter.withPhysicalMeter(physicalMeter));
    }

    physicalMeterUseCases.save(physicalMeter);
    measurementUseCases.save(measurements);
    return referenceInfoDtoBuilder.build();
  }

  @Override
  public Optional<MeteringResponseDto> handle(MeteringAlarmMessageDto alarmMessage) {
    // TODO we should handle incoming alarms
    return Optional.empty();
  }

  MeterDefinition selectMeterDefinition(List<ValueDto> values) {
    boolean isDistrictHeatingMeter = values
      .stream()
      .map(valueDto -> valueDto.quantity)
      .collect(toList())
      .containsAll(DISTRICT_HEATING_METER_QUANTITIES);
    return isDistrictHeatingMeter
      ? MeterDefinition.DISTRICT_HEATING_METER
      : MeterDefinition.UNKNOWN_METER;
  }

  private Quantity mappedQuantity(String quantity) {
    return METER_TO_MVP_QUANTITIES.getOrDefault(quantity, new Quantity(quantity));
  }

  private boolean facilityIdIsInvalid(String id) {
    return id.trim().isEmpty();
  }

  private Gateway findOrCreateGateway(
    Organisation organisation,
    LogicalMeter logicalMeter,
    String serial,
    String productModel
  ) {
    return gatewayUseCases.findBy(organisation.id, productModel, serial)
      .orElseGet(() ->
        gatewayUseCases.findBy(organisation.id, serial).orElseGet(() ->
          new Gateway(
            randomUUID(),
            organisation.id,
            serial,
            productModel,
            singletonList(logicalMeter),
            emptyList() // TODO Save gateway status
          )));
  }

  private Organisation findOrCreateOrganisation(String externalId) {
    return organisationUseCases.findByExternalId(externalId)
      .orElseGet(() ->
        organisationUseCases.create(new Organisation(randomUUID(), externalId)
          .withExternalId(externalId)));
  }

  private PhysicalMeter findOrCreatePhysicalMeter(
    String facilityId,
    String meterId,
    String medium,
    String manufacturer,
    UUID logicalMeterId,
    Organisation organisation
  ) {
    return physicalMeterUseCases.findByOrganisationIdAndExternalIdAndAddress(
      organisation.id,
      facilityId,
      meterId
    ).orElseGet(() ->
      new PhysicalMeter(
        randomUUID(),
        organisation,
        meterId,
        facilityId,
        medium,
        manufacturer,
        logicalMeterId,
        0L,
        null
      )
    );
  }
}
