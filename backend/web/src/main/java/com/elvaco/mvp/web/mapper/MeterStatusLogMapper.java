package com.elvaco.mvp.web.mapper;

import java.util.TimeZone;

import com.elvaco.mvp.core.domainmodels.MeterStatusLog;
import com.elvaco.mvp.web.dto.MeterStatusLogDto;
import com.elvaco.mvp.web.util.Dates;

public class MeterStatusLogMapper {

  public MeterStatusLogDto toDto(MeterStatusLog meterStatusLog, TimeZone timeZone) {

    MeterStatusLogDto meterStatusLogDto = new MeterStatusLogDto();
    meterStatusLogDto.statusId = meterStatusLog.statusId;
    meterStatusLogDto.start = Dates.formatTime(meterStatusLog.start, timeZone);

    if (meterStatusLog.stop != null) {
      meterStatusLogDto.stop = Dates.formatTime(meterStatusLog.stop, timeZone);
    } else {
      meterStatusLogDto.stop = "";
    }
    
    meterStatusLogDto.name = meterStatusLog.name;

    return meterStatusLogDto;
  }

}
