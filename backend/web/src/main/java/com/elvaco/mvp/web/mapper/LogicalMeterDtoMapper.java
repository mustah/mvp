package com.elvaco.mvp.web.mapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.util.Dates;
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
    meterDto.status = logicalMeter.currentStatus();
    meterDto.manufacturer = logicalMeter.getManufacturer();
    meterDto.statusChanged = Optional.ofNullable(logicalMeter.currentStatus)
      .map(logEntry -> Dates.formatUtc(logEntry.start))
      .orElse(null);
    meterDto.facility = logicalMeter.externalId;
    meterDto.address = logicalMeter.activePhysicalMeter()
      .map(m -> m.address)
      .orElse(null);

    meterDto.collectionPercentage = logicalMeter.getCollectionPercentage()
      .orElse(null);

    meterDto.gatewaySerial =
      logicalMeter.gateways
        .stream()
        .findFirst()
        .map(gateway -> gateway.serial)
        .orElse(null);

    meterDto.location = toLocationDto(logicalMeter.location);

    meterDto.readIntervalMinutes = logicalMeter.activePhysicalMeter()
      .map(m -> m.readIntervalMinutes)
      .orElse(null);

    meterDto.organisationId = logicalMeter.organisationId;

    return meterDto;
  }

  public static LogicalMeterDto toDto(LogicalMeter logicalMeter) {
    String created = formatUtc(logicalMeter.created);
    LogicalMeterDto meterDto = new LogicalMeterDto();
    meterDto.medium = logicalMeter.getMedium();
    meterDto.created = created;
    meterDto.id = logicalMeter.id;
    meterDto.status = logicalMeter.currentStatus();
    meterDto.flags = emptyList();
    meterDto.manufacturer = logicalMeter.getManufacturer();
    meterDto.statusChanged = Dates.formatUtc(Optional.ofNullable(logicalMeter.currentStatus)
      .map(uuidStatusLogEntry -> uuidStatusLogEntry.start)
      .orElse(logicalMeter.created));
    meterDto.facility = logicalMeter.externalId;
    meterDto.address = logicalMeter.activePhysicalMeter()
      .map(m -> m.address)
      .orElse(null);

    meterDto.collectionPercentage = logicalMeter.getCollectionPercentage()
      .orElse(null);

    meterDto.gateway =
      logicalMeter.gateways
        .stream()
        .findFirst()
        .map(GatewayDtoMapper::toGatewayMandatory)
        .orElse(null);

    meterDto.location = toLocationDto(logicalMeter.location);

    meterDto.readIntervalMinutes = logicalMeter.activePhysicalMeter()
      .map(m -> m.readIntervalMinutes)
      .orElse(null);

    meterDto.measurements = logicalMeter.latestReadouts
      .stream()
      .map(MeasurementDtoMapper::toDto)
      .collect(toList());

    meterDto.statusChangelog = getMeterStatusLogs(logicalMeter)
      .stream()
      .map(MeterStatusLogDtoMapper::toDto)
      .collect(toList());

    meterDto.organisationId = logicalMeter.organisationId;

    return meterDto;
  }

  private static List<StatusLogEntry<UUID>> getMeterStatusLogs(LogicalMeter logicalMeter) {
    return logicalMeter.physicalMeters.stream()
      .flatMap(physicalMeter -> physicalMeter.statuses.stream())
      .collect(toList());
  }
}
