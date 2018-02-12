package com.elvaco.mvp.web.mapper;

import com.elvaco.mvp.core.domainmodels.MeterStatusLog;
import com.elvaco.mvp.web.dto.MeterStatusLogDto;

public class MeterStatusLogMapper {

  public MeterStatusLogDto toDto(MeterStatusLog meterStatusLog) {

    MeterStatusLogDto meterStatusLogDto = new MeterStatusLogDto();
    meterStatusLogDto.start = meterStatusLog.start;
    meterStatusLogDto.stop = meterStatusLog.stop;
    meterStatusLogDto.name = meterStatusLog.name;

    return meterStatusLogDto;
  }

}
