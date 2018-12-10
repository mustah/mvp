package com.elvaco.mvp.web.mapper;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.CollectionStats;
import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.dto.LogicalMeterSummaryDto;
import com.elvaco.mvp.core.util.Dates;
import com.elvaco.mvp.web.dto.AlarmDto;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.MapMarkerWithStatusDto;
import com.elvaco.mvp.web.dto.PagedLogicalMeterDto;

import lombok.experimental.UtilityClass;

import static com.elvaco.mvp.core.util.Dates.formatUtc;
import static com.elvaco.mvp.web.mapper.LocationDtoMapper.toLocationDto;
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

  public PagedLogicalMeterDto toPagedDto(LogicalMeterSummaryDto logicalMeter) {
    var meterDto = new PagedLogicalMeterDto();
    meterDto.id = logicalMeter.id;
    meterDto.organisationId = logicalMeter.organisationId;
    meterDto.medium = logicalMeter.medium;
    meterDto.manufacturer = logicalMeter.manufacturer;
    meterDto.facility = logicalMeter.externalId;
    meterDto.address = logicalMeter.address;
    meterDto.gatewaySerial = logicalMeter.gatewaySerial;

    meterDto.alarm = Optional.ofNullable(logicalMeter.activeAlarm)
      .map(alarm -> new AlarmDto(alarm.id, alarm.mask))
      .orElse(null);

    meterDto.isReported = Optional.ofNullable(logicalMeter.activeStatus)
      .filter(StatusType::isNotUnknown)
      .map(StatusType::isReported)
      .orElse(false);

    meterDto.readIntervalMinutes = logicalMeter.readIntervalMinutes;
    meterDto.collectionPercentage = CollectionStats.of(
      logicalMeter.missingReadingCount,
      logicalMeter.expectedReadingCount
    ).getCollectionPercentage();

    meterDto.location = toLocationDto(logicalMeter.location);

    return meterDto;
  }

  public static LogicalMeterDto toDto(LogicalMeter logicalMeter) {
    String created = formatUtc(logicalMeter.created);
    Optional<StatusLogEntry> statusLog = logicalMeter.activeStatusLog();
    LogicalMeterDto meterDto = new LogicalMeterDto();
    meterDto.id = logicalMeter.id;
    meterDto.medium = logicalMeter.getMedium();
    meterDto.created = created;
    meterDto.isReported = statusLog.map(entry -> entry.status.isReported())
      .orElse(false);
    meterDto.statusChanged = Dates.formatUtc(statusLog.map(status -> status.start)
      .orElse(logicalMeter.created));
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

    meterDto.gateway = logicalMeter.gateways.stream()
      .findFirst()
      .map(GatewayDtoMapper::toGatewayMandatory)
      .orElse(null);

    meterDto.location = toLocationDto(logicalMeter.location);

    meterDto.statusChangelog = getMeterStatusLogs(logicalMeter).stream()
      .map(MeterStatusLogDtoMapper::toDto)
      .collect(toList());

    meterDto.alarm = Optional.ofNullable(logicalMeter.alarm)
      .map(alarm -> new AlarmDto(alarm.id, alarm.mask, alarm.description))
      .orElse(null);

    meterDto.organisationId = logicalMeter.organisationId;

    return meterDto;
  }

  private static List<StatusLogEntry> getMeterStatusLogs(LogicalMeter logicalMeter) {
    return logicalMeter.physicalMeters.stream()
      .flatMap(physicalMeter -> physicalMeter.statuses.stream())
      .collect(toList());
  }
}
