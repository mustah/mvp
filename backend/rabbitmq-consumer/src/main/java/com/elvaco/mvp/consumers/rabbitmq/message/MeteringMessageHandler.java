package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.elvaco.mvp.consumers.rabbitmq.dto.GatewayStatusDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringAlarmMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeterStructureMessageDto;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

public class MeteringMessageHandler implements MessageHandler {

  private final Map<String, MeterDefinition> mediumToMeterDefinitionMap;
  private final LogicalMeterUseCases logicalMeterUseCases;
  private final PhysicalMeterUseCases physicalMeterUseCases;
  private final OrganisationUseCases organisationUseCases;
  private final MeasurementUseCases measurementUseCases;
  private final GatewayUseCases gatewayUseCases;

  public MeteringMessageHandler(
    LogicalMeterUseCases logicalMetersUseCases,
    PhysicalMeterUseCases physicalMeterUseCases,
    OrganisationUseCases organisationUseCases,
    MeasurementUseCases measurementUseCases,
    GatewayUseCases gatewayUseCases
  ) {
    this.logicalMeterUseCases = logicalMetersUseCases;
    this.physicalMeterUseCases = physicalMeterUseCases;
    this.organisationUseCases = organisationUseCases;
    this.measurementUseCases = measurementUseCases;
    this.gatewayUseCases = gatewayUseCases;
    this.mediumToMeterDefinitionMap = newMediumToMeterDefinitionMap();
  }

  @Override
  public void handle(MeteringMeterStructureMessageDto structureMessage) {
    Organisation organisation = findOrCreateOrganisation(structureMessage.organisationId);

    LogicalMeter logicalMeter = findOrCreateLogicalMeter(
      structureMessage.facility.id,
      structureMessage.meter.medium,
      organisation
    );

    PhysicalMeter physicalMeter = findOrCreatePhysicalMeter(
      structureMessage.facility.id,
      structureMessage.meter.id,
      structureMessage.meter.medium,
      structureMessage.meter.manufacturer,
      logicalMeter.id,
      organisation
    ).withMedium(structureMessage.meter.medium)
      .withManufacturer(structureMessage.meter.manufacturer)
      .withLogicalMeterId(logicalMeter.id);
    physicalMeterUseCases.save(physicalMeter);

    GatewayStatusDto gateway = structureMessage.gateway;

    gatewayUseCases.findBy(organisation.id, gateway.productModel, gateway.id)
      .orElseGet(() -> {
        // TODO[!must!] create and save gateway when the logical meter is created (after demo)
        // TODO[!must!] this still works, but it does one extra round-trip to DB
        Gateway g = new Gateway(
          UUID.randomUUID(),
          organisation.id,
          gateway.id,
          gateway.productModel,
          singletonList(logicalMeter)
        );
        Gateway saved = gatewayUseCases.save(g);

        logicalMeterUseCases.save(logicalMeter.withGateway(saved));

        return saved;
      });
  }

  @Override
  public void handle(MeteringMeasurementMessageDto measurementMessage) {
    Organisation organisation = findOrCreateOrganisation(measurementMessage.organisationId);

    PhysicalMeter physicalMeter = findOrCreatePhysicalMeter(
      measurementMessage.facility.id,
      measurementMessage.meter.id,
      "Unknown",
      "UNKNOWN",
      null,
      organisation
    );

    List<Measurement> measurements = measurementMessage.values
      .stream()
      .map(valueDto -> new Measurement(
        null,
        // Note: Metering stores and treats all values as CET - at least it's consistent!
        valueDto.timestamp.atZone(ZoneId.of("CET")),
        valueDto.quantity,
        valueDto.value,
        valueDto.unit,
        physicalMeter
      ))
      .collect(toList());
    measurementUseCases.save(measurements);
  }

  @Override
  public void handle(MeteringAlarmMessageDto alarmMessage) {
    // TODO we should handle incoming alarms
  }

  private LogicalMeter findOrCreateLogicalMeter(
    String facilityId,
    String medium,
    Organisation organisation
  ) {
    return logicalMeterUseCases.findByOrganisationIdAndExternalId(
      organisation.id,
      facilityId
    ).orElseGet(() -> logicalMeterUseCases.save(
      new LogicalMeter(
        randomUUID(),
        facilityId,
        organisation.id,
        selectMeterDefinition(medium)
      )));
  }

  private Organisation findOrCreateOrganisation(String organisationCode) {
    return organisationUseCases.findByCode(organisationCode)
      .orElseGet(() ->
                   organisationUseCases.create(
                     new Organisation(
                       UUID.randomUUID(),
                       organisationCode,
                       organisationCode
                     )));
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
      () -> physicalMeterUseCases.save(
        new PhysicalMeter(
          UUID.randomUUID(),
          organisation,
          meterId,
          facilityId,
          medium,
          manufacturer,
          logicalMeterId,
          0L, // TODO add real interval here!
          null
        ))
    );
  }

  private MeterDefinition selectMeterDefinition(String medium) {
    return mediumToMeterDefinitionMap.getOrDefault(medium, MeterDefinition.UNKNOWN_METER);
  }

  private Map<String, MeterDefinition> newMediumToMeterDefinitionMap() {
    Map<String, MeterDefinition> map = new HashMap<>();
    map.put("Hot water", MeterDefinition.HOT_WATER_METER);
    return map;
  }
}
