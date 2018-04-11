package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.mappers.MeterStatusLogMapper;

public class MeterStatusLogsRepository implements MeterStatusLogs {

  private final PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository;
  private final MeterStatusLogMapper meterStatusLogMapper;

  public MeterStatusLogsRepository(
    PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository,
    MeterStatusLogMapper meterStatusLogMapper
  ) {
    this.physicalMeterStatusLogJpaRepository = physicalMeterStatusLogJpaRepository;
    this.meterStatusLogMapper = meterStatusLogMapper;
  }

  @Override
  public StatusLogEntry<UUID> save(StatusLogEntry<UUID> meterStatusLog) {
    return meterStatusLogMapper.toDomainModel(physicalMeterStatusLogJpaRepository.save(
      meterStatusLogMapper.toEntity(meterStatusLog)));
  }

  @Override
  public void save(List<StatusLogEntry<UUID>> meterStatusLogs) {
    physicalMeterStatusLogJpaRepository.save(
      meterStatusLogs.stream().map(meterStatusLogMapper::toEntity).collect(Collectors.toList())
    );
  }
}
