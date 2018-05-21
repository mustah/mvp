package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.consumers.rabbitmq.dto.FacilityDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.GatewayStatusDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeterDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringStructureMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.helpers.CronHelper;
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

    if (facility == null || facility.id == null || facility.id.trim().isEmpty()) {
      log.warn("Discarding message with invalid facility id: '{}'", structureMessage);
      return;
    }

    MeterDto meterDto = structureMessage.meter;

    Location location = new LocationBuilder()
      .country(facility.country)
      .city(facility.city)
      .address(facility.address)
      .build();

    LogicalMeter logicalMeter = findOrCreateLogicalMeter(
      meterDto,
      location,
      organisation.id,
      facility.id
    );

    Optional<PhysicalMeter> physicalMeter = Optional.ofNullable(meterDto)
      .flatMap(meter ->
        findOrCreatePhysicalMeter(
          meter,
          organisation,
          facility,
          logicalMeter
        ));

    Optional<Gateway> gateway = Optional.ofNullable(findOrCreateGateway(
      structureMessage.gateway,
      logicalMeter,
      organisation.id
    ));

    gateway.ifPresent(gatewayUseCases::save);

    Optional.ofNullable(logicalMeter)
      .map(meter -> physicalMeter.map(meter::withPhysicalMeter).orElse(meter))
      .map(meter -> gateway.map(meter::withGateway).orElse(meter))
      .map(logicalMeterUseCases::save)
      .ifPresent(meter -> geocodeService.fetchCoordinates(
        LocationWithId.of(meter.location, meter.id))
      );
  }

  @Nullable
  private LogicalMeter findOrCreateLogicalMeter(
    @Nullable MeterDto meterDto,
    Location location,
    UUID organisationId,
    String facilityId
  ) {
    return logicalMeterUseCases.findByOrganisationIdAndExternalId(organisationId, facilityId)
      .map(logicalMeter -> logicalMeter.withLocation(location))
      .orElseGet(() ->
        Optional.ofNullable(meterDto)
          .map(meter -> new LogicalMeter(
            randomUUID(),
            facilityId,
            organisationId,
            MeterDefinition.fromMedium(Medium.from(meter.medium)),
            location
          ))
          .orElse(null));
  }

  private Optional<PhysicalMeter> findOrCreatePhysicalMeter(
    MeterDto meterDto,
    Organisation organisation,
    FacilityDto facility,
    @Nullable LogicalMeter logicalMeter
  ) {
    String address = meterDto.id;
    if (address == null) {
      return Optional.empty();
    }

    PhysicalMeter physicalMeter = physicalMeterUseCases
      .findByOrganisationIdAndExternalIdAndAddress(organisation.id, facility.id, address)
      .orElseGet(() ->
        PhysicalMeter.builder()
          .organisation(organisation)
          .address(address)
          .externalId(facility.id)
          .build());

    physicalMeter = physicalMeter.withMedium(meterDto.medium)
      .withManufacturer(meterDto.manufacturer)
      .replaceActiveStatus(StatusType.from(meterDto.status))
      .withReadIntervalMinutes(CronHelper.toReportInterval(meterDto.cron)
        .map(Duration::toMinutes)
        .orElse(null));

    if (logicalMeter != null) {
      physicalMeter = physicalMeter.withLogicalMeterId(logicalMeter.id);
    }

    return Optional.of(physicalMeter);
  }

  @Nullable
  private Gateway findOrCreateGateway(
    @Nullable GatewayStatusDto gatewayStatusDto,
    @Nullable LogicalMeter logicalMeter,
    UUID organisationId
  ) {
    if (gatewayStatusDto != null && gatewayStatusDto.id != null && logicalMeter != null) {
      return gatewayUseCases.findBy(
        organisationId,
        gatewayStatusDto.productModel,
        gatewayStatusDto.id
      ).orElseGet(() ->
        gatewayUseCases.findBy(organisationId, gatewayStatusDto.id)
          .orElseGet(() ->
            new Gateway(
              randomUUID(),
              organisationId,
              gatewayStatusDto.id,
              gatewayStatusDto.productModel,
              singletonList(logicalMeter),
              emptyList() // TODO Save gateway status
            ))
      )
        .withProductModel(gatewayStatusDto.productModel)
        .replaceActiveStatus(StatusType.from(gatewayStatusDto.status));
    } else {
      return null;
    }
  }
}
