package com.elvaco.mvp.consumers.rabbitmq.message;

import java.util.UUID;

import com.elvaco.mvp.consumers.rabbitmq.dto.FacilityDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringStructureMessageDto;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LocationWithId;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.spi.geocode.GeocodeService;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;

@Slf4j
@AllArgsConstructor
public class MeteringStructureMessageConsumer implements StructureMessageConsumer {

  private final LogicalMeterUseCases logicalMeterUseCases;
  private final PhysicalMeterUseCases physicalMeterUseCases;
  private final OrganisationUseCases organisationUseCases;
  private final GatewayUseCases gatewayUseCases;
  private final GeocodeService geocodeService;

  @Override
  public void accept(MeteringStructureMessageDto structureMessage) {
    Organisation organisation = organisationUseCases.findOrCreate(structureMessage.organisationId);
    FacilityDto facility = structureMessage.facility;

    if (facility.id.trim().isEmpty()) {
      log.warn("Discarding message with invalid facility id: '{}'", structureMessage);
      return;
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

  private Gateway findOrCreateGateway(
    Organisation organisation,
    LogicalMeter logicalMeter,
    String serial,
    String productModel
  ) {
    return gatewayUseCases.findBy(organisation.id, productModel, serial)
      .orElseGet(() ->
        gatewayUseCases.findBy(organisation.id, serial)
          .orElseGet(() ->
            new Gateway(
              randomUUID(),
              organisation.id,
              serial,
              productModel,
              singletonList(logicalMeter),
              emptyList() // TODO Save gateway status
            )));
  }
}