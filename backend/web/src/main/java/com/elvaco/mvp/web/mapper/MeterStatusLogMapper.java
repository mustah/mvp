package com.elvaco.mvp.web.mapper;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.web.dto.MeterStatusLogDto;

import static com.elvaco.mvp.core.util.Dates.formatUtc;

public class MeterStatusLogMapper {

  public MeterStatusLogDto toDto(StatusLogEntry<UUID> meterStatusLog) {
    MeterStatusLogDto meterStatusLogDto = new MeterStatusLogDto();
    meterStatusLogDto.id = meterStatusLog.id;
    meterStatusLogDto.name = meterStatusLog.status.name;
    meterStatusLogDto.start = formatUtc(meterStatusLog.start);
    meterStatusLogDto.stop = meterStatusLog.stop != null ? formatUtc(meterStatusLog.stop) : "";
    return meterStatusLogDto;
  }
}
