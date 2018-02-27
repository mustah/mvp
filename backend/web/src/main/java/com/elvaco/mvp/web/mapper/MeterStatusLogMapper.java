package com.elvaco.mvp.web.mapper;

import java.util.Optional;
import java.util.TimeZone;

import com.elvaco.mvp.core.domainmodels.MeterStatusLog;
import com.elvaco.mvp.web.dto.MeterStatusLogDto;
import com.elvaco.mvp.web.util.Dates;

public class MeterStatusLogMapper {

  public MeterStatusLogDto toDto(MeterStatusLog meterStatusLog, TimeZone timeZone) {
    MeterStatusLogDto meterStatusLogDto = new MeterStatusLogDto();
    meterStatusLogDto.id = meterStatusLog.id;
    meterStatusLogDto.statusId = meterStatusLog.statusId;
    meterStatusLogDto.start = Dates.formatTime(meterStatusLog.start, timeZone);
    meterStatusLogDto.name = meterStatusLog.name;
    meterStatusLogDto.stop = Optional.ofNullable(meterStatusLog.stop)
      .map(stop -> Dates.formatTime(meterStatusLog.stop, timeZone))
      .orElse("");

    return meterStatusLogDto;
  }
}
