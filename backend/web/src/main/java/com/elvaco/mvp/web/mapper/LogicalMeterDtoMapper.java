package com.elvaco.mvp.web.mapper;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.dto.LogicalMeterSummaryDto;
import com.elvaco.mvp.core.util.Dates;
import com.elvaco.mvp.web.dto.AlarmDto;
import com.elvaco.mvp.web.dto.EventLogDto;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.MapMarkerWithStatusDto;
import com.elvaco.mvp.web.dto.PagedLogicalMeterDto;

import lombok.experimental.UtilityClass;

import static com.elvaco.mvp.core.util.Dates.formatUtc;
import static com.elvaco.mvp.web.mapper.LocationDtoMapper.toLocationDto;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static java.util.Comparator.reverseOrder;
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

  public static PagedLogicalMeterDto toPagedDto(LogicalMeterSummaryDto logicalMeter) {
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
    meterDto.collectionPercentage = Optional.ofNullable(logicalMeter.collectionPercentage)
      .orElseGet(() ->
        logicalMeter.readIntervalMinutes == null || logicalMeter.readIntervalMinutes == 0
          ? null
          : 0.0);

    meterDto.location = toLocationDto(logicalMeter.location);

    return meterDto;
  }

  public static LogicalMeterDto toDto(LogicalMeter logicalMeter) {
    return toDto(logicalMeter, ZonedDateTime.now());
  }

  private static LogicalMeterDto toDto(LogicalMeter logicalMeter, ZonedDateTime when) {
    String created = formatUtc(logicalMeter.created);
    Optional<StatusLogEntry> statusLog = logicalMeter.activeStatusLog();
    LogicalMeterDto meterDto = new LogicalMeterDto();
    meterDto.id = logicalMeter.id;
    meterDto.medium = logicalMeter.getMedium().name;
    meterDto.created = created;
    meterDto.isReported = statusLog.map(entry -> entry.status.isReported())
      .orElse(false);
    meterDto.statusChanged = Dates.formatUtc(statusLog.map(status -> status.start)
      .orElse(logicalMeter.created));
    meterDto.manufacturer = logicalMeter.getManufacturer();
    meterDto.facility = logicalMeter.externalId;

    Optional<PhysicalMeter> physicalMeter = logicalMeter.activePhysicalMeter(when);
    meterDto.address = physicalMeter
      .map(m -> m.address)
      .orElse(null);
    meterDto.readIntervalMinutes = physicalMeter
      .map(m -> m.readIntervalMinutes)
      .orElse(null);
    meterDto.mbusDeviceType = physicalMeter
      .map(m -> m.mbusDeviceType)
      .orElse(null);
    meterDto.revision = physicalMeter
      .map(m -> m.revision)
      .orElse(null);

    meterDto.gateway = logicalMeter.gateways.stream()
      .min(comparing(gw -> gw.lastSeen, nullsLast(reverseOrder())))
      .map(GatewayDtoMapper::toGatewayMandatory)
      .orElse(null);

    meterDto.location = toLocationDto(logicalMeter.location);

    meterDto.eventLog = getEventLog(logicalMeter);

    meterDto.alarms = logicalMeter.alarms
      .stream()
      .map(alarm -> new AlarmDto(alarm.id, alarm.mask, alarm.description))
      .collect(toList());

    meterDto.organisationId = logicalMeter.organisationId;

    return meterDto;
  }

  private static List<EventLogDto> getEventLog(LogicalMeter logicalMeter) {
    List<EventLogDto> events = new ArrayList<>();

    logicalMeter.physicalMeters.stream()
      .peek(physicalMeter -> events.add(EventLogDtoMapper.toMeterReplacementDto(physicalMeter)))
      .flatMap(physicalMeter -> physicalMeter.statuses.stream())
      .map(EventLogDtoMapper::toStatusChangeDto)
      .forEach(events::add);

    events.sort(comparing((EventLogDto eventLogDto) -> eventLogDto.start).reversed());
    return events;
  }
}
