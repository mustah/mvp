package com.elvaco.mvp.consumers.rabbitmq.message;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeterStructureMessageDto;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

public class MeteringMessageHandler implements MessageHandler {

  private final Map<String, MeterDefinition> mediumToMeterDefinitionMap;
  private final LogicalMeterUseCases logicalMeterUseCases;
  private final PhysicalMeterUseCases physicalMeterUseCases;
  private final OrganisationUseCases organisationUseCases;
  private final MeasurementUseCases measurementUseCases;

  public MeteringMessageHandler(
    LogicalMeterUseCases logicalMetersUseCases,
    PhysicalMeterUseCases physicalMeterUseCases,
    OrganisationUseCases organisationUseCases,
    MeasurementUseCases measurementUseCases
  ) {
    this.logicalMeterUseCases = logicalMetersUseCases;
    this.physicalMeterUseCases = physicalMeterUseCases;
    this.organisationUseCases = organisationUseCases;
    this.measurementUseCases = measurementUseCases;
    mediumToMeterDefinitionMap = newMediumToMeterDefinitionMap();
  }

  @Override
  public void handle(MeteringMeterStructureMessageDto structureMessage) {
    Organisation organisation = findOrCreateOrganisation(structureMessage.organisationId);

    LogicalMeter logicalMeter = findOrCreateLogicalMeter(
      structureMessage.facilityId,
      structureMessage.medium,
      organisation
    );

    PhysicalMeter physicalMeter = findOrCreatePhysicalMeter(
      structureMessage.facilityId,
      structureMessage.meterId,
      structureMessage.medium,
      structureMessage.manufacturer,
      logicalMeter.id,
      organisation
    ).withMedium(structureMessage.medium)
      .withManufacturer(structureMessage.manufacturer)
      .withLogicalMeterId(logicalMeter.id);
    physicalMeterUseCases.save(physicalMeter);
  }

  @Override
  public void handle(MeteringMeasurementMessageDto measurementMessage) {
    Organisation organisation = findOrCreateOrganisation(measurementMessage.organisationId);

    PhysicalMeter physicalMeter = findOrCreatePhysicalMeter(
      measurementMessage.facilityId,
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
        new Date(valueDto.timestamp),
        valueDto.quantity,
        valueDto.value,
        valueDto.unit,
        physicalMeter
      ))
      .collect(toList());
    measurementUseCases.save(measurements);
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
                       "",
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
          emptyList()
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
