package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import com.elvaco.mvp.consumers.rabbitmq.dto.FacilityDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringAlarmMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeterStructureMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringResponseDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationWithId;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.spi.geocode.GeocodeService;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;
import lombok.extern.slf4j.Slf4j;

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Slf4j
public class MeteringMessageHandler implements MessageHandler {

  private static final List<String> DISTRICT_HEATING_METER_QUANTITIES = unmodifiableList(asList(
    "Return temp.",
    "Difference temp.",
    "Flow temp.",
    "Volume flow",
    "Power",
    "Volume",
    "Energy"
  ));
  private final Map<String, MeterDefinition> mediumToMeterDefinitionMap;
  private final LogicalMeterUseCases logicalMeterUseCases;
  private final PhysicalMeterUseCases physicalMeterUseCases;
  private final OrganisationUseCases organisationUseCases;
  private final MeasurementUseCases measurementUseCases;
  private final GatewayUseCases gatewayUseCases;

  private final GeocodeService geocodeService;

  public MeteringMessageHandler(
    LogicalMeterUseCases logicalMetersUseCases,
    PhysicalMeterUseCases physicalMeterUseCases,
    OrganisationUseCases organisationUseCases,
    MeasurementUseCases measurementUseCases,
    GatewayUseCases gatewayUseCases,
    GeocodeService geocodeService
  ) {
    this.logicalMeterUseCases = logicalMetersUseCases;
    this.physicalMeterUseCases = physicalMeterUseCases;
    this.organisationUseCases = organisationUseCases;
    this.measurementUseCases = measurementUseCases;
    this.gatewayUseCases = gatewayUseCases;
    this.geocodeService = geocodeService;
    this.mediumToMeterDefinitionMap = newMediumToMeterDefinitionMap();
  }

  @Override
  public Optional<MeteringResponseDto> handle(MeteringMeterStructureMessageDto structureMessage) {
    Organisation organisation = findOrCreateOrganisation(structureMessage.organisationId);
    FacilityDto facility = structureMessage.facility;

    if (facilityIdIsInvalid(facility.id)) {
      log.warn("Discarding message with invalid facility/external ID '{}'", facility.id);
      return Optional.empty();
    }

    LogicalMeter logicalMeter = logicalMeterUseCases
      .findByOrganisationIdAndExternalId(organisation.id, facility.id)
      .orElseGet(() ->
        new LogicalMeter(
          randomUUID(),
          facility.id,
          organisation.id,
          selectMeterDefinition(structureMessage.meter.medium),
          new Location(facility.country, facility.city, facility.address)
        )).withLocation(new Location(facility.country, facility.city, facility.address));

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
      .replaceActiveStatus(
        StatusType.from(structureMessage.meter.status)
      );

    Gateway gateway = findOrCreateGateway(
      organisation,
      logicalMeter,
      structureMessage.gateway.id,
      structureMessage.gateway.productModel
    ).withProductModel(structureMessage.gateway.productModel);

    gatewayUseCases.save(gateway);

    LogicalMeter meter = logicalMeter
      .withGateway(gateway)
      .withPhysicalMeter(physicalMeter);

    logicalMeterUseCases.save(meter);

    physicalMeterUseCases.save(physicalMeter);

    geocodeService.fetchCoordinates(LocationWithId.from(meter.location, meter.id));

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
      ).orElseGet(
        () -> {
          referenceInfoDtoBuilder.setMeterExternalId(facilityId);
          return new LogicalMeter(
            randomUUID(),
            facilityId,
            organisation.id,
            selectMeterDefinition(medium),
            UNKNOWN_LOCATION
          );
        }
      );

    PhysicalMeter physicalMeter = physicalMeterUseCases.findByOrganisationIdAndExternalIdAndAddress(
      organisation.id,
      facilityId,
      measurementMessage.meter.id
    ).orElseGet(
      () -> {
        referenceInfoDtoBuilder.setMeterExternalId(facilityId);
        return new PhysicalMeter(
          UUID.randomUUID(),
          organisation,
          measurementMessage.meter.id,
          facilityId,
          medium,
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
          UUID.randomUUID(),
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
      .map(
        valueDto -> measurementUseCases
          .findForMeterCreatedAt(
            physicalMeter.id,
            valueDto.quantity,
            valueDto.timestamp.atZone(ZoneId.of("CET"))
          ).orElseGet(() -> new Measurement(
            null,
            // Note: Metering stores and treats all values as CET - at least
            // it's consistent!
            valueDto.timestamp.atZone(ZoneId.of("CET")),
            valueDto.quantity,
            valueDto.value,
            valueDto.unit,
            physicalMeter
          )).withValue(valueDto.value)
          .withUnit(valueDto.unit)
          .withQuantity(valueDto.quantity)
      )
      .collect(toList());

    if (optionalGateway.isPresent()) {
      Gateway gateway = optionalGateway.get();
      gatewayUseCases.save(optionalGateway.get());
      logicalMeterUseCases.save(
        logicalMeter.withGateway(gateway)
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

  MeterDefinition selectMeterDefinition(String medium) {
    return mediumToMeterDefinitionMap.getOrDefault(medium, MeterDefinition.UNKNOWN_METER);
  }

  MeterDefinition selectMeterDefinition(List<ValueDto> values) {
    boolean isDistrictHeatingMeter = values
      .stream()
      .map(valueDto -> valueDto.quantity)
      .collect(toList())
      .containsAll(DISTRICT_HEATING_METER_QUANTITIES);
    if (isDistrictHeatingMeter) {
      return MeterDefinition.DISTRICT_HEATING_METER;
    }
    return MeterDefinition.UNKNOWN_METER;
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
      .orElseGet(
        () -> gatewayUseCases.findBy(organisation.id, serial).orElseGet(() ->
          new Gateway(
            UUID.randomUUID(),
            organisation.id,
            serial,
            productModel,
            singletonList(logicalMeter),
            emptyList() // TODO Save gateway status
          )));
  }

  private LogicalMeter findOrCreateLogicalMeter(
    FacilityDto facility,
    String medium,
    Organisation organisation
  ) {
    return logicalMeterUseCases.findByOrganisationIdAndExternalId(
      organisation.id,
      facility.id
    ).orElseGet(() ->
      new LogicalMeter(
        randomUUID(),
        facility.id,
        organisation.id,
        selectMeterDefinition(medium),
        new Location(facility.country, facility.city, facility.address)
      ));
  }

  private Organisation findOrCreateOrganisation(String externalId) {
    return organisationUseCases.findByExternalId(externalId)
      .orElseGet(() ->
        organisationUseCases.create(
          new Organisation(
            UUID.randomUUID(),
            externalId
          ).withExternalId(externalId)));
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
    ).orElseGet(
      () ->
        new PhysicalMeter(
          UUID.randomUUID(),
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

  private Map<String, MeterDefinition> newMediumToMeterDefinitionMap() {
    Map<String, MeterDefinition> map = new HashMap<>();
    map.put("Hot water", MeterDefinition.HOT_WATER_METER);
    map.putAll(Stream.of(
      MeterDefinition.DISTRICT_HEATING_METER,
      MeterDefinition.HOT_WATER_METER,
      MeterDefinition.UNKNOWN_METER,
      MeterDefinition.DISTRICT_COOLING_METER
    ).collect(toMap((meterDefinition) -> meterDefinition.medium, Function.identity())));
    return map;
  }
}
