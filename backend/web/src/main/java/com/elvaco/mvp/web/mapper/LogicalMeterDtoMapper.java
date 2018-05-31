package com.elvaco.mvp.web.mapper;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.util.Dates;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.MapMarkerWithStatusDto;
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
        getCurrentStatus(getMeterStatusLogs(logicalMeter)).name,
        coord.getLatitude(),
        coord.getLongitude()
      );
    }
    return null;
  }

  public static LogicalMeterDto toDto(LogicalMeter logicalMeter) {
    List<StatusLogEntry<UUID>> statusLogs = getMeterStatusLogs(logicalMeter);

    String created = formatUtc(logicalMeter.created);
    LogicalMeterDto meterDto = new LogicalMeterDto();
    meterDto.medium = logicalMeter.getMedium();
    meterDto.created = created;
    meterDto.id = logicalMeter.id;
    meterDto.status = getCurrentStatus(statusLogs);
    meterDto.flags = emptyList();
    meterDto.manufacturer = logicalMeter.getManufacturer();
    meterDto.statusChanged = statusLogs.stream()
      .findFirst()
      .map(meterStatusLog -> meterStatusLog.start)
      .map(Dates::formatUtc)
      .orElse(created);
    meterDto.facility = logicalMeter.externalId;
    meterDto.address = logicalMeter.activePhysicalMeter()
      .map(m -> m.address)
      .orElse(null);

    meterDto.collectionPercentage = logicalMeter.getCollectionPercentage()
      .orElse(null);

    meterDto.gateway = logicalMeter.gateways
      .stream()
      .findFirst()
      .map(GatewayDtoMapper::toGatewayMandatory)
      .orElse(null);

    meterDto.location = toLocationDto(logicalMeter.location);

    meterDto.readIntervalMinutes = logicalMeter.readIntervalMinutes;

    meterDto.measurements = logicalMeter.measurements
      .stream()
      .map(MeasurementDtoMapper::toDto)
      .collect(toList());

    meterDto.statusChangelog = statusLogs
      .stream()
      .map(MeterStatusLogDtoMapper::toDto)
      .collect(toList());

    meterDto.organisationId = logicalMeter.organisationId;

    return meterDto;
  }

  private static StatusType getCurrentStatus(List<StatusLogEntry<UUID>> statusLogs) {
    return statusLogs.stream()
      .findFirst()
      .map(meterStatusLog -> meterStatusLog.status)
      .orElse(StatusType.UNKNOWN);
  }

  private static List<StatusLogEntry<UUID>> getMeterStatusLogs(LogicalMeter logicalMeter) {
    return logicalMeter.physicalMeters.stream()
      .flatMap(physicalMeter -> physicalMeter.statuses.stream())
      .collect(toList());
  }
}
