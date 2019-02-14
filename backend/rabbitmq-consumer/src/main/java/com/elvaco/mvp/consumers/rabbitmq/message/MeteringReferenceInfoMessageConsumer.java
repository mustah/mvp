package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.consumers.rabbitmq.helpers.CronHelper;
import com.elvaco.mvp.core.access.MediumProvider;
import com.elvaco.mvp.core.domainmodels.FeatureType;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LocationWithId;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.spi.amqp.JobService;
import com.elvaco.mvp.core.spi.geocode.GeocodeService;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MeterDefinitionUseCases;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;
import com.elvaco.mvp.core.usecase.PropertiesUseCases;
import com.elvaco.mvp.core.util.StatusLogEntryHelper;
import com.elvaco.mvp.producers.rabbitmq.dto.FacilityDto;
import com.elvaco.mvp.producers.rabbitmq.dto.GatewayStatusDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeterDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringReferenceInfoMessageDto;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.DEFAULT_READ_INTERVAL_MINUTES;
import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.mapToEvoMedium;

@Slf4j
@AllArgsConstructor
public class MeteringReferenceInfoMessageConsumer implements ReferenceInfoMessageConsumer {

  private final LogicalMeterUseCases logicalMeterUseCases;
  private final PhysicalMeterUseCases physicalMeterUseCases;
  private final OrganisationUseCases organisationUseCases;
  private final GatewayUseCases gatewayUseCases;
  private final GeocodeService geocodeService;
  private final PropertiesUseCases propertiesUseCases;
  private final JobService<MeteringReferenceInfoMessageDto> meterSyncJobService;
  private final MediumProvider mediumProvider;
  private final MeterDefinitionUseCases meterDefinitionUseCases;

  @Override
  public void accept(MeteringReferenceInfoMessageDto message) {
    if (meterSyncJobService.isActive(message.jobId)) {
      meterSyncJobService.update(message.jobId, message);
    }

    FacilityDto facility = message.facility;

    if (facility == null || facility.id == null || facility.id.trim().isEmpty()) {
      if (message.gateway != null
        && message.gateway.id != null
        && !message.gateway.id.trim().isEmpty()) {
        updateGateway(message);
        return;
      } else {
        log.warn("Discarding message with invalid facility id: '{}'", message);
        return;
      }
    }

    logInvalidStatus(message);

    Organisation organisation = organisationUseCases.findOrCreate(message.organisationId);

    Location location = new LocationBuilder()
      .country(facility.country)
      .city(facility.city)
      .address(facility.address)
      .zip(facility.zip)
      .build();

    MeterDto meterDto = message.meter;
    LogicalMeter logicalMeter = findOrCreateLogicalMeter(
      meterDto,
      location,
      organisation,
      facility.id
    );

    Optional<PhysicalMeter> physicalMeter = Optional.ofNullable(meterDto)
      .filter(meter -> Objects.nonNull(meter.id))
      .map(meter -> findOrCreatePhysicalMeter(
        meter,
        facility.id,
        organisation.id,
        logicalMeter != null ? logicalMeter.id : null
      ));

    Optional<Gateway> gateway = Optional.ofNullable(findOrCreateGateway(
      message.gateway,
      logicalMeter,
      organisation.id
    ));

    Optional.ofNullable(logicalMeter)
      .map(logicalMeterUseCases::save)
      .ifPresent(this::onFetchCoordinates);

    physicalMeter.ifPresent(physicalMeterUseCases::saveWithStatuses);
    gateway.ifPresent(gatewayUseCases::save);
  }

  private void updateGateway(MeteringReferenceInfoMessageDto message) {
    Organisation organisation = organisationUseCases.findOrCreate(message.organisationId);
    GatewayStatusDto gwDto = message.gateway;
    Optional<Gateway> gw = gatewayUseCases.findBy(organisation.id, message.gateway.id);
    gw.map(g -> g.toBuilder()
      .phoneNumber(gwDto.phoneNumber)
      .ip(gwDto.ip)
      .productModel(gwDto.productModel).build())
      .map(gatewayUseCases::save);
  }

  private void logInvalidStatus(MeteringReferenceInfoMessageDto message) {
    MeterDto meterDto = message.meter;
    if (meterDto != null && meterDto.id != null
      && !StatusType.from(meterDto.status).isNotUnknown()) {
      log.warn("Received message with invalid status type: '{}'", message);
    }
  }

  private void onFetchCoordinates(LogicalMeter meter) {
    LocationWithId location = LocationBuilder.from(meter.location)
      .id(meter.id)
      .shouldForceUpdate(propertiesUseCases.shouldUpdateGeolocation(
        meter.id,
        meter.organisationId
      ))
      .buildLocationWithId();

    geocodeService.fetchCoordinates(location);

    removePropertyAfterForceUpdate(location, meter.organisationId);
  }

  private void removePropertyAfterForceUpdate(LocationWithId location, UUID organisationId) {
    if (location.shouldForceUpdate) {
      propertiesUseCases.deleteBy(
        FeatureType.UPDATE_GEOLOCATION,
        location.getId().getId(),
        organisationId
      );
    }
  }

  @Nullable
  private LogicalMeter findOrCreateLogicalMeter(
    @Nullable MeterDto meterDto,
    Location location,
    Organisation organisation,
    String facilityId
  ) {
    Optional<MeterDto> meter = Optional.ofNullable(meterDto);

    MeterDefinition meterDefinition = meter.map(
      dto -> meterDefinitionUseCases.getAutoApplied(
        organisation,
        mapToEvoMedium(mediumProvider, dto.medium)
      )
    ).orElse(MeterDefinition.UNKNOWN);

    // TODO: if utcOffset change we do not recalculate historical measurement_stat.
    return logicalMeterUseCases.findBy(organisation.id, facilityId)
      .map(logicalMeter -> logicalMeter.toBuilder()
        .location(location)
        .meterDefinition(meterDefinition)
        .build())
      .orElseGet(() -> meter.map(dto -> LogicalMeter.builder()
        .externalId(facilityId)
        .organisationId(organisation.id)
        .meterDefinition(meterDefinition)
        .location(location)
        .build())
        .orElse(null));
  }

  private PhysicalMeter findOrCreatePhysicalMeter(
    MeterDto meterDto,
    String facilityId,
    UUID organisationId,
    @Nullable UUID logicalMeterId
  ) {
    PhysicalMeter physicalMeter = physicalMeterUseCases
      .findByWithStatuses(organisationId, facilityId, meterDto.id)
      .orElseGet(() -> PhysicalMeter.builder()
        .organisationId(organisationId)
        .address(meterDto.id)
        .readIntervalMinutes(DEFAULT_READ_INTERVAL_MINUTES)
        .externalId(facilityId)
        .build()
      );

    List<StatusLogEntry> statuses = StatusLogEntryHelper.replaceActiveStatus(
      List.copyOf(physicalMeter.statuses),
      StatusLogEntry.builder()
        .primaryKey(physicalMeter.primaryKey())
        .status(StatusType.from(meterDto.status))
        .build()
    );

    return physicalMeter.toBuilder()
      .medium(meterDto.medium)
      .manufacturer(meterDto.manufacturer)
      .revision(meterDto.revision)
      .mbusDeviceType(meterDto.mbusDeviceType)
      .readIntervalMinutes(readInterval(meterDto.cron, physicalMeter))
      .activePeriod(physicalMeter.activePeriod)
      .logicalMeterId(logicalMeterId != null ? logicalMeterId : physicalMeter.logicalMeterId)
      .build()
      .setStatuses(statuses);
  }

  private Long readInterval(String cron, PhysicalMeter physicalMeter) {
    return CronHelper.toReportInterval(cron)
      .map(Duration::toMinutes)
      .map(d -> d == 0 ? DEFAULT_READ_INTERVAL_MINUTES : d)
      .orElse(physicalMeter.readIntervalMinutes);
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
      ).orElseGet(() -> gatewayUseCases.findBy(organisationId, gatewayStatusDto.id)
        .orElseGet(() -> Gateway.builder()
          .organisationId(organisationId)
          .serial(gatewayStatusDto.id)
          .productModel(gatewayStatusDto.productModel)
          .meter(logicalMeter)
          .ip(gatewayStatusDto.ip)
          .phoneNumber(gatewayStatusDto.phoneNumber)
          .build()
        ))
        .toBuilder()
        .productModel(gatewayStatusDto.productModel)
        .ip(gatewayStatusDto.ip)
        .phoneNumber(gatewayStatusDto.phoneNumber)
        .build()
        .replaceActiveStatus(StatusType.from(gatewayStatusDto.status));
    } else {
      return null;
    }
  }
}
