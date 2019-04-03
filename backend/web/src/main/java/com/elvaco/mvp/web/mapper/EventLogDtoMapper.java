package com.elvaco.mvp.web.mapper;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.util.Dates;
import com.elvaco.mvp.web.dto.EventLogDto;
import com.elvaco.mvp.web.dto.EventType;

import lombok.experimental.UtilityClass;

import static com.elvaco.mvp.core.util.Dates.formatUtc;

@UtilityClass
public class EventLogDtoMapper {

  public static EventLogDto toStatusChangeDto(StatusLogEntry meterStatusLog) {
    return EventLogDto.builder()
      .type(EventType.statusChange)
      .name(meterStatusLog.status.name)
      .start(formatUtc(meterStatusLog.start))
      .build();
  }

  static EventLogDto toMeterReplacementDto(PhysicalMeter physicalMeter) {
    return EventLogDto.builder()
      .type(EventType.newMeter)
      .name(physicalMeter.address)
      .start(physicalMeter.activePeriod.getStartDateTime().map(Dates::formatUtc).orElse("-∞"))
      .build();
  }
}
