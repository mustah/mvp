package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.mappers.MeterStatusLogMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MeterStatusLogsRepository implements MeterStatusLogs {

  private final PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository;

  @Override
  public StatusLogEntry<UUID> save(StatusLogEntry<UUID> meterStatusLog) {
    return MeterStatusLogMapper.toDomainModel(physicalMeterStatusLogJpaRepository.save(
      MeterStatusLogMapper.toEntity(meterStatusLog)));
  }

  @Override
  public void save(List<StatusLogEntry<UUID>> meterStatusLogs) {
    physicalMeterStatusLogJpaRepository.save(
      meterStatusLogs.stream().map(MeterStatusLogMapper::toEntity).collect(Collectors.toList())
    );
  }
}
