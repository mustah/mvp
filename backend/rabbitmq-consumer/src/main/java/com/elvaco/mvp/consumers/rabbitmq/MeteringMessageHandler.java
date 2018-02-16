package com.elvaco.mvp.consumers.rabbitmq;

import com.elvaco.mvp.consumers.rabbitmq.message.MeteringMeterStructureMessageDto;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;

class MeteringMessageHandler {
  private PhysicalMeters physicalMeters;
  private Organisations organisations;

  MeteringMessageHandler(PhysicalMeters physicalMeters, Organisations organisations) {
    this.physicalMeters = physicalMeters;
    this.organisations = organisations;
  }

  public void handle(MeteringMeterStructureMessageDto structureMessage) {
    Organisation organisation = organisations.findByCode(structureMessage.organisationId)
      .orElseGet(() -> organisations.save(
        new Organisation(
          null,
          "",
          structureMessage.organisationId
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
    ).orElse(
      new PhysicalMeter(
        organisation,
        structureMessage.meterId,
        structureMessage.facilityId,
        structureMessage.medium,
        structureMessage.manufacturer
      )
    );

    physicalMeters.save(physicalMeter);
  }
}
