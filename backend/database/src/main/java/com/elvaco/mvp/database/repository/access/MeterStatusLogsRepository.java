package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.stream.Collectors;

import com.elvaco.mvp.core.domainmodels.MeterStatusLog;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.mappers.MeterStatusLogMapper;

public class MeterStatusLogsRepository implements MeterStatusLogs {

  public final PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository;
  public final MeterStatusLogMapper meterStatusLogMapper;

  public MeterStatusLogsRepository(
    PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository,
    MeterStatusLogMapper meterStatusLogMapper
  ) {
    this.physicalMeterStatusLogJpaRepository = physicalMeterStatusLogJpaRepository;
    this.meterStatusLogMapper = meterStatusLogMapper;
  }

  @Override
  public void save(MeterStatusLog meterStatusLog) {
    physicalMeterStatusLogJpaRepository.save(
      meterStatusLogMapper.toEntity(meterStatusLog)
    );
  }

  @Override
  public void save(List<MeterStatusLog> meterStatusLogs) {
    physicalMeterStatusLogJpaRepository.save(
      meterStatusLogs.stream().map(meterStatusLogMapper::toEntity).collect(Collectors.toList())
    );
  }

}
