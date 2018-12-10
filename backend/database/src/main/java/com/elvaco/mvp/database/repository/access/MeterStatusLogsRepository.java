package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Objects;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.mappers.MeterStatusLogEntityMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MeterStatusLogsRepository implements MeterStatusLogs {

  private final PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository;

  @Override
  public StatusLogEntry save(StatusLogEntry meterStatusLog) {
    return MeterStatusLogEntityMapper.toDomainModel(physicalMeterStatusLogJpaRepository.save(
      MeterStatusLogEntityMapper.toEntity(meterStatusLog)));
  }

  @Override
  public void save(List<StatusLogEntry> meterStatusLogs) {
    meterStatusLogs.stream()
      .filter(uuidStatusLogEntry -> Objects.nonNull(uuidStatusLogEntry.start))
      .map(MeterStatusLogEntityMapper::toEntity)
      .forEach(physicalMeterStatusLogJpaRepository::save);
  }
}
