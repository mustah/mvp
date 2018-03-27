package com.elvaco.mvp.web.mapper;

import java.util.Collection;
import java.util.List;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterStatusLog;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.util.Dates;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.MapMarkerDto;

import static com.elvaco.mvp.core.util.Dates.formatUtc;
import static com.elvaco.mvp.web.mapper.LocationMapper.toLocationDto;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class LogicalMeterMapper {

  private static final String NO_PERCENTAGE = "";

  private final MeterStatusLogMapper meterStatusLogMapper;
  private final GatewayMapper gatewayMapper;

  public LogicalMeterMapper(
    MeterStatusLogMapper meterStatusLogMapper,
    GatewayMapper gatewayMapper
  ) {
    this.meterStatusLogMapper = meterStatusLogMapper;
    this.gatewayMapper = gatewayMapper;
  }

  public MapMarkerDto toMapMarkerDto(LogicalMeter logicalMeter) {
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
    List<MeterStatusLog> statusLogs = getMeterStatusLogs(logicalMeter);

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

    meterDto.collectionStatus = logicalMeter.getCollectionPercentage()
      .map(val -> String.valueOf(val * 100))
      .orElse(NO_PERCENTAGE);

    meterDto.gateway = logicalMeter.gateways
      .stream()
      .findFirst()
      .map(gatewayMapper::toGatewayMandatory)
      .orElse(null);

    meterDto.location = toLocationDto(logicalMeter.location);

    meterDto.statusChangelog = statusLogs
      .stream()
      .map(meterStatusLogMapper::toDto)
      .collect(toList());
    return meterDto;
  }

  private StatusType getCurrentStatus(List<MeterStatusLog> statusLogs) {
    return statusLogs.stream()
      .findFirst()
      .map(meterStatusLog -> meterStatusLog.status)
      .orElse(StatusType.UNKNOWN);
  }

  private List<MeterStatusLog> getMeterStatusLogs(LogicalMeter logicalMeter) {
    return logicalMeter.physicalMeters.stream()
      .map(physicalMeter -> physicalMeter.statuses)
      .flatMap(Collection::stream)
      .collect(toList());
  }
}
