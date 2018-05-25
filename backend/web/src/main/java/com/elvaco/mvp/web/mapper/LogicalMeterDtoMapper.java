package com.elvaco.mvp.web.mapper;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.util.Dates;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.MapMarkerDto;
import lombok.RequiredArgsConstructor;

import static com.elvaco.mvp.core.util.Dates.formatUtc;
import static com.elvaco.mvp.web.mapper.LocationDtoMapper.toLocationDto;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class LogicalMeterDtoMapper {

  private final MeterStatusLogDtoMapper meterStatusLogDtoMapper;
  private final GatewayDtoMapper gatewayDtoMapper;
  private final MeasurementDtoMapper measurementDtoMapper;

  public static MapMarkerDto toMapMarkerDto(LogicalMeter logicalMeter) {
    MapMarkerDto mapMarkerDto = new MapMarkerDto();
    mapMarkerDto.id = logicalMeter.id;
    mapMarkerDto.status = getCurrentStatus(getMeterStatusLogs(logicalMeter)).name;
    if (logicalMeter.location.hasCoordinates()) {
      GeoCoordinate coord = logicalMeter.location.getCoordinate();
      if (coord != null) {
        mapMarkerDto.confidence = coord.getConfidence();
        mapMarkerDto.latitude = coord.getLatitude();
        mapMarkerDto.longitude = coord.getLongitude();
      }
    }
    return mapMarkerDto;
  }

  public LogicalMeterDto toDto(LogicalMeter logicalMeter) {
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
      .map(gatewayDtoMapper::toGatewayMandatory)
      .orElse(null);

    meterDto.location = toLocationDto(logicalMeter.location);

    meterDto.readIntervalMinutes = logicalMeter.readIntervalMinutes;

    meterDto.measurements = logicalMeter.measurements
      .stream()
      .map(measurementDtoMapper::toDto)
      .collect(toList());

    meterDto.statusChangelog = statusLogs
      .stream()
      .map(meterStatusLogDtoMapper::toDto)
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
      .map(physicalMeter -> physicalMeter.statuses)
      .flatMap(Collection::stream)
      .collect(toList());
  }
}
