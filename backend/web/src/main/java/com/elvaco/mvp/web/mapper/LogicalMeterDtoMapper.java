package com.elvaco.mvp.web.mapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.util.Dates;
import com.elvaco.mvp.web.dto.AlarmDto;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.MapMarkerWithStatusDto;
import com.elvaco.mvp.web.dto.PagedLogicalMeterDto;
import lombok.experimental.UtilityClass;

import static com.elvaco.mvp.core.util.Dates.formatUtc;
import static com.elvaco.mvp.web.mapper.LocationDtoMapper.toLocationDto;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@UtilityClass
public class LogicalMeterDtoMapper {

  @Nullable
  public static MapMarkerWithStatusDto toMapMarkerDto(LogicalMeter logicalMeter) {
    Location location = logicalMeter.location;
    if (location.hasHighConfidence()) {
      GeoCoordinate coord = location.getCoordinate();
      return new MapMarkerWithStatusDto(
        logicalMeter.id,
        logicalMeter.currentStatus().name,
        coord.getLatitude(),
        coord.getLongitude()
      );
    }
    return null;
  }

  public PagedLogicalMeterDto toPagedDto(LogicalMeter logicalMeter) {
    PagedLogicalMeterDto meterDto = new PagedLogicalMeterDto();
    meterDto.medium = logicalMeter.getMedium();
    meterDto.id = logicalMeter.id;
    meterDto.alarm = Optional.ofNullable(logicalMeter.alarm)
      .map(alarm -> new AlarmDto(alarm.id, alarm.mask))
      .orElse(null);
    meterDto.manufacturer = logicalMeter.getManufacturer();
    meterDto.facility = logicalMeter.externalId;

    Optional<PhysicalMeter> physicalMeter = logicalMeter.activePhysicalMeter();
    meterDto.address = physicalMeter
      .map(m -> m.address)
      .orElse(null);
    meterDto.readIntervalMinutes = physicalMeter
      .map(m -> m.readIntervalMinutes)
      .orElse(null);

    meterDto.collectionPercentage = logicalMeter.getCollectionPercentage();

    meterDto.gatewaySerial =
      logicalMeter.gateways
        .stream()
        .findFirst()
        .map(gateway -> gateway.serial)
        .orElse(null);

    meterDto.location = toLocationDto(logicalMeter.location);

    meterDto.organisationId = logicalMeter.organisationId;

    return meterDto;
  }

  public static PagedLogicalMeterDto toPagedDetailsDto(LogicalMeter logicalMeter) {
    PagedLogicalMeterDto pagedMeterDto = toPagedDto(logicalMeter);
    Optional<StatusLogEntry<UUID>> statusLog = logicalMeter.activeStatusLog();
    pagedMeterDto.status = statusLog.map(entry -> entry.status).orElse(StatusType.UNKNOWN);
    pagedMeterDto.statusChanged = Dates.formatUtc(statusLog.map(status -> status.start)
      .orElse(logicalMeter.created));
    return pagedMeterDto;
  }

  public static LogicalMeterDto toDto(LogicalMeter logicalMeter) {
    String created = formatUtc(logicalMeter.created);
    Optional<StatusLogEntry<UUID>> statusLog = logicalMeter.activeStatusLog();
    LogicalMeterDto meterDto = new LogicalMeterDto();
    meterDto.medium = logicalMeter.getMedium();
    meterDto.created = created;
    meterDto.id = logicalMeter.id;
    meterDto.status = statusLog.map(entry -> entry.status).orElse(StatusType.UNKNOWN);
    meterDto.statusChanged = Dates.formatUtc(statusLog.map(status -> status.start)
      .orElse(logicalMeter.created));
    meterDto.flags = emptyList();
    meterDto.manufacturer = logicalMeter.getManufacturer();
    meterDto.facility = logicalMeter.externalId;

    Optional<PhysicalMeter> physicalMeter = logicalMeter.activePhysicalMeter();
    meterDto.address = physicalMeter
      .map(m -> m.address)
      .orElse(null);
    meterDto.readIntervalMinutes = physicalMeter
      .map(m -> m.readIntervalMinutes)
      .orElse(null);

    meterDto.collectionPercentage = logicalMeter.getCollectionPercentage();

    meterDto.gateway =
      logicalMeter.gateways.stream()
        .findFirst()
        .map(GatewayDtoMapper::toGatewayMandatory)
        .orElse(null);

    meterDto.location = toLocationDto(logicalMeter.location);

    meterDto.measurements = logicalMeter.latestReadouts.stream()
      .map(MeasurementDtoMapper::toDto)
      .collect(toList());

    meterDto.statusChangelog = getMeterStatusLogs(logicalMeter).stream()
      .map(MeterStatusLogDtoMapper::toDto)
      .collect(toList());

    meterDto.alarm = Optional.ofNullable(logicalMeter.alarm)
      .map(alarm -> new AlarmDto(alarm.id, alarm.mask, alarm.description))
      .orElse(null);

    meterDto.organisationId = logicalMeter.organisationId;

    return meterDto;
  }

  private static List<StatusLogEntry<UUID>> getMeterStatusLogs(LogicalMeter logicalMeter) {
    return logicalMeter.physicalMeters.stream()
      .flatMap(physicalMeter -> physicalMeter.statuses.stream())
      .collect(toList());
  }
}
