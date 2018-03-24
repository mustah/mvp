package com.elvaco.mvp.web.mapper;

import java.util.TimeZone;

import com.elvaco.mvp.core.domainmodels.MeterStatusLog;
import com.elvaco.mvp.web.dto.MeterStatusLogDto;
import com.elvaco.mvp.web.util.Dates;

public class MeterStatusLogMapper {

  public MeterStatusLogDto toDto(MeterStatusLog meterStatusLog, TimeZone timeZone) {
    MeterStatusLogDto meterStatusLogDto = new MeterStatusLogDto();
    meterStatusLogDto.id = meterStatusLog.id;
    meterStatusLogDto.name = meterStatusLog.status.name;
    meterStatusLogDto.start = Dates.formatTime(meterStatusLog.start, timeZone);
    meterStatusLogDto.stop = meterStatusLog.stop != null
      ? Dates.formatTime(meterStatusLog.stop, timeZone)
      : "";
    return meterStatusLogDto;
  }
}
