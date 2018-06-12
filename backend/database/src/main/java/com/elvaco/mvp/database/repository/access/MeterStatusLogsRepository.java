package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.mappers.MeterStatusLogEntityMapper;
import lombok.RequiredArgsConstructor;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class MeterStatusLogsRepository implements MeterStatusLogs {

  private final PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository;

  @Override
  public StatusLogEntry<UUID> save(StatusLogEntry<UUID> meterStatusLog) {
    return MeterStatusLogEntityMapper.toDomainModel(physicalMeterStatusLogJpaRepository.save(
      MeterStatusLogEntityMapper.toEntity(meterStatusLog)));
  }

  @Override
  public void save(List<StatusLogEntry<UUID>> meterStatusLogs) {
    physicalMeterStatusLogJpaRepository.save(
      meterStatusLogs.stream()
        .map(MeterStatusLogEntityMapper::toEntity)
        .collect(toList())
    );
  }
}
