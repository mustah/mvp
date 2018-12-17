package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.consumers.rabbitmq.helpers.CronHelper;
import com.elvaco.mvp.core.domainmodels.FeatureType;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LocationWithId;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.spi.amqp.JobService;
import com.elvaco.mvp.core.spi.geocode.GeocodeService;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;
import com.elvaco.mvp.core.usecase.PropertiesUseCases;
import com.elvaco.mvp.producers.rabbitmq.dto.FacilityDto;
import com.elvaco.mvp.producers.rabbitmq.dto.GatewayStatusDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeterDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringReferenceInfoMessageDto;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.mapToEvoMedium;

@Slf4j
@AllArgsConstructor
public class MeteringReferenceInfoMessageConsumer implements ReferenceInfoMessageConsumer {

  private static final Long DEFAULT_READ_INTERVAL_MINUTES = 60L;

  private final LogicalMeterUseCases logicalMeterUseCases;
  private final PhysicalMeterUseCases physicalMeterUseCases;
  private final OrganisationUseCases organisationUseCases;
  private final GatewayUseCases gatewayUseCases;
  private final GeocodeService geocodeService;
  private final PropertiesUseCases propertiesUseCases;
  private final JobService<MeteringReferenceInfoMessageDto> meterSyncJobService;

  @Override
  public void accept(MeteringReferenceInfoMessageDto referenceInfoMessage) {
    FacilityDto facility = referenceInfoMessage.facility;

    String jobId = referenceInfoMessage.jobId;
    if (meterSyncJobService.isActive(jobId)) {
      meterSyncJobService.update(jobId, referenceInfoMessage);
    }

    if (facility == null || facility.id == null || facility.id.trim().isEmpty()) {
      log.warn("Discarding message with invalid facility id: '{}'", referenceInfoMessage);
      return;
    }

    Organisation organisation =
      organisationUseCases.findOrCreate(referenceInfoMessage.organisationId);

    MeterDto meterDto = referenceInfoMessage.meter;

    Location location = new LocationBuilder()
      .country(facility.country)
      .city(facility.city)
      .address(facility.address)
      .build();

    LogicalMeter logicalMeter = findOrCreateLogicalMeter(
      meterDto,
      location,
      organisation.id,
      facility.id,
      "+01" //TODO: facility.utcOffset when metering supports that
    );

    Optional<PhysicalMeter> physicalMeter = Optional.ofNullable(meterDto)
      .flatMap(meter ->
        findOrCreatePhysicalMeter(
          meter,
          facility.id,
          logicalMeter,
          organisation.id
        ));

    Optional<Gateway> gateway = Optional.ofNullable(findOrCreateGateway(
      referenceInfoMessage.gateway,
      logicalMeter,
      organisation.id
    ));

    Optional.ofNullable(logicalMeter)
      .map(logicalMeterUseCases::save)
      .ifPresent(this::onFetchCoordinates);

    physicalMeter.ifPresent(physicalMeterUseCases::saveWithStatuses);
    gateway.ifPresent(gatewayUseCases::save);
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
    UUID organisationId,
    String facilityId,
    String utcOffset
  ) {
    Optional<MeterDto> meter = Optional.ofNullable(meterDto);

    MeterDefinition meterDefinition = meter
      .map(dto -> MeterDefinition.fromMedium(mapToEvoMedium(dto.medium)))
      .orElse(MeterDefinition.UNKNOWN_METER);

    return logicalMeterUseCases.findBy(organisationId, facilityId)
      .map(logicalMeter -> logicalMeter.toBuilder()
        .location(location)
        .meterDefinition(meterDefinition)
        //NOTE: if utcOffset change we do not recalculate historical measurement_stat.
        .utcOffset(utcOffset)
        .build())
      .orElseGet(() -> meter.map(dto -> LogicalMeter.builder()
        .externalId(facilityId)
        .organisationId(organisationId)
        .meterDefinition(meterDefinition)
        .location(location)
        .utcOffset(utcOffset)
        .build())
        .orElse(null));
  }

  private Optional<PhysicalMeter> findOrCreatePhysicalMeter(
    MeterDto meterDto,
    String facilityId,
    @Nullable LogicalMeter logicalMeter,
    UUID organisationId
  ) {
    String address = meterDto.id;
    if (address == null) {
      return Optional.empty();
    }

    PhysicalMeter physicalMeter = physicalMeterUseCases
      .findByWithStatuses(organisationId, facilityId, address)
      .orElseGet(() -> PhysicalMeter.builder()
        .organisationId(organisationId)
        .address(address)
        .readIntervalMinutes(DEFAULT_READ_INTERVAL_MINUTES)
        .externalId(facilityId)
        .build()
      );

    return Optional.of(
      physicalMeter.toBuilder()
        .medium(meterDto.medium)
        .manufacturer(meterDto.manufacturer)
        .revision(meterDto.revision)
        .mbusDeviceType(meterDto.mbusDeviceType)
        .readIntervalMinutes(readInterval(meterDto, physicalMeter))
        .activePeriod(physicalMeter.activePeriod)
        .logicalMeterId(Optional.ofNullable(logicalMeter)
          .map(LogicalMeter::getId)
          .orElse(physicalMeter.logicalMeterId))
        .build()
        .replaceActiveStatus(StatusType.from(meterDto.status), ZonedDateTime.now())
    );
  }

  private Long readInterval(MeterDto meterDto, PhysicalMeter physicalMeter) {
    return CronHelper.toReportInterval(meterDto.cron)
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
      ).orElseGet(() ->
        gatewayUseCases.findBy(organisationId, gatewayStatusDto.id)
          .orElseGet(() ->
            Gateway.builder()
              .organisationId(organisationId)
              .serial(gatewayStatusDto.id)
              .productModel(gatewayStatusDto.productModel)
              .meter(logicalMeter)
              .build()
          )
      )
        .toBuilder()
        .productModel(gatewayStatusDto.productModel)
        .build()
        .replaceActiveStatus(StatusType.from(gatewayStatusDto.status));
    } else {
      return null;
    }
  }
}
