package com.elvaco.mvp.consumers.rabbitmq.message;

import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.consumers.rabbitmq.dto.FacilityDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.GatewayStatusDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeterDto;
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

    Optional<LogicalMeter> logicalMeter = findOrCreateLogicalMeter(
      structureMessage.meter,
      organisation,
      facility,
      location
    );

    Optional<PhysicalMeter> physicalMeter = Optional.empty();
    if (structureMessage.meter != null) {
      physicalMeter = Optional.of(findOrCreatePhysicalMeter(
        structureMessage.meter,
        organisation,
        facility,
        logicalMeter.orElse(null)
      ));
    }

    Optional<Gateway> gateway = findOrCreateGateway(
      structureMessage.gateway,
      organisation,
      logicalMeter.orElse(null)
    );

    if (gateway.isPresent()) {
      gatewayUseCases.save(gateway.get());
    }

    if (logicalMeter.isPresent()) {
      LogicalMeter meter = logicalMeter.get();

      if (physicalMeter.isPresent() && meter != null) {
        meter = meter.withPhysicalMeter(physicalMeter.get());
      }

      if (gateway.isPresent()) {
        meter = meter.withGateway(gateway.get());
      }

      logicalMeterUseCases.save(meter);
      if (meter != null) {
        geocodeService.fetchCoordinates(LocationWithId.of(meter.location, meter.id));
      }
    }
  }

  private Optional<LogicalMeter> findOrCreateLogicalMeter(
    @Nullable MeterDto meterDto,
    Organisation organisation,
    FacilityDto facility,
    Location location
  ) {
    LogicalMeter logicalMeter = logicalMeterUseCases
      .findByOrganisationIdAndExternalId(organisation.id, facility.id).orElse(null);

    if (meterDto != null && logicalMeter == null) {
      logicalMeter = new LogicalMeter(
        randomUUID(),
        facility.id,
        organisation.id,
        MeterDefinition.fromMedium(Medium.from(meterDto.medium)),
        location
      );
    }

    if (logicalMeter != null) {
      return Optional.of(logicalMeter.withLocation(location));
    }

    return Optional.empty();
  }

  private PhysicalMeter findOrCreatePhysicalMeter(
    MeterDto meterDto,
    Organisation organisation,
    FacilityDto facility,
    @Nullable LogicalMeter logicalMeter
  ) {
    String address = meterDto.id;
    Integer expectedInterval = meterDto.expectedInterval;

    PhysicalMeter physicalMeter = physicalMeterUseCases
      .findByOrganisationIdAndExternalIdAndAddress(organisation.id, facility.id, address)
      .orElse(
        PhysicalMeter.builder()
          .organisation(organisation)
          .address(address)
          .externalId(facility.id)
          .build()
      );

    physicalMeter = physicalMeter.withMedium(meterDto.medium)
      .withManufacturer(meterDto.manufacturer)
      .replaceActiveStatus(StatusType.from(meterDto.status))
      .withReadIntervalMinutes(expectedInterval);

    if (logicalMeter != null) {
      physicalMeter = physicalMeter.withLogicalMeterId(logicalMeter.id);
    }

    return physicalMeter;
  }

  private Optional<Gateway> findOrCreateGateway(
    @Nullable GatewayStatusDto gatewayStatusDto,
    Organisation organisation,
    @Nullable LogicalMeter logicalMeter
  ) {
    if (gatewayStatusDto != null && logicalMeter != null) {
      return Optional.of(findOrCreateGateway(
        organisation,
        logicalMeter,
        gatewayStatusDto.id,
        gatewayStatusDto.productModel
      ).withProductModel(gatewayStatusDto.productModel)
                           .replaceActiveStatus(StatusType.from(gatewayStatusDto.status)));
    }

    return Optional.empty();
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
