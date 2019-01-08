package com.elvaco.mvp.web.mapper;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.web.dto.EventLogDto;

import lombok.experimental.UtilityClass;

import static com.elvaco.mvp.core.util.Dates.formatUtc;

@UtilityClass
public class MeterStatusLogDtoMapper {

  public static EventLogDto toDto(StatusLogEntry meterStatusLog) {
    EventLogDto eventLogDto = new EventLogDto();
    eventLogDto.id = meterStatusLog.id;
    eventLogDto.name = meterStatusLog.status.name;
    eventLogDto.start = formatUtc(meterStatusLog.start);
    return eventLogDto;
  }
}
