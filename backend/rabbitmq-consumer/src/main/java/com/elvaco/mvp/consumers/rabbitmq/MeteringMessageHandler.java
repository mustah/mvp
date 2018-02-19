package com.elvaco.mvp.consumers.rabbitmq;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMeterStructureMessageDto;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;

class MeteringMessageHandler {

  private final Map<String, MeterDefinition> mediumToMeterDefinitionMap;
  private final LogicalMeters logicalMeters;
  private final PhysicalMeters physicalMeters;
  private final Organisations organisations;

  MeteringMessageHandler(
    LogicalMeters logicalMeters,
    PhysicalMeters physicalMeters,
    Organisations organisations
  ) {
    this.logicalMeters = logicalMeters;
    this.physicalMeters = physicalMeters;
    this.organisations = organisations;
    mediumToMeterDefinitionMap = newMediumToMeterDefinitionMap();
  }

  public void handle(MeteringMeterStructureMessageDto structureMessage) {
    Organisation organisation = organisations.findByCode(structureMessage.organisationId)
      .orElseGet(() -> organisations.save(
        new Organisation(
          null,
          "",
          structureMessage.organisationId
        )));

    LogicalMeter logicalMeter = logicalMeters.findByOrganisationIdAndExternalId(
      organisation.id,
      structureMessage.facilityId
    )
      .orElseGet(() -> logicalMeters.save(
        new LogicalMeter(
          structureMessage.facilityId,
          organisation.id,
          selectMeterDefinition(structureMessage.medium)
        )));

    PhysicalMeter physicalMeter = physicalMeters.findByOrganisationIdAndExternalIdAndAddress(
      organisation.id,
      structureMessage.facilityId,
      structureMessage.meterId
    ).map(meter ->
            new PhysicalMeter(
              meter.id,
              meter.organisation,
              meter.address,
              meter.externalId,
              structureMessage.medium,
              structureMessage.manufacturer
            )
    ).orElse(new PhysicalMeter(
      null, organisation,
      structureMessage.meterId,
      structureMessage.facilityId,
      structureMessage.medium,
      structureMessage.manufacturer,
      logicalMeter.id,
      Collections.emptyList()
    ));
    physicalMeters.save(physicalMeter);
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
