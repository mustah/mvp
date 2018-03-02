package com.elvaco.mvp.consumers.rabbitmq.message;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeterStructureMessageDto;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class MeteringMessageHandler implements MessageHandler {

  private final Map<String, MeterDefinition> mediumToMeterDefinitionMap;
  private final LogicalMeters logicalMeters;
  private final PhysicalMeters physicalMeters;
  private final Organisations organisations;
  private final MeasurementUseCases measurementUseCases;

  public MeteringMessageHandler(
    LogicalMeters logicalMeters,
    PhysicalMeters physicalMeters,
    Organisations organisations,
    MeasurementUseCases measurementUseCases
  ) {
    this.logicalMeters = logicalMeters;
    this.physicalMeters = physicalMeters;
    this.organisations = organisations;
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
    physicalMeters.save(physicalMeter);
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
    return logicalMeters.findByOrganisationIdAndExternalId(
      organisation.id,
      facilityId
    ).orElseGet(() -> logicalMeters.save(
      new LogicalMeter(
        facilityId,
        organisation.id,
        selectMeterDefinition(medium)
      )));
  }

  private Organisation findOrCreateOrganisation(String organisationCode) {
    return organisations.findByCode(organisationCode)
      .orElseGet(() ->
                   organisations.save(
                     new Organisation(
                       null,
                       "",
                       organisationCode
                     )));
  }

  private PhysicalMeter findOrCreatePhysicalMeter(
    String facilityId,
    String meterId,
    String medium, String manufacturer, Long logicalMeterId, Organisation organisation
  ) {
    return physicalMeters.findByOrganisationIdAndExternalIdAndAddress(
      organisation.id,
      facilityId,
      meterId
    ).orElseGet(
      () -> physicalMeters.save(
        new PhysicalMeter(
          null,
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
