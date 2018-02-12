package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.elvaco.mvp.adapters.spring.PageAdapter;
import com.elvaco.mvp.core.domainmodels.MeterStatusLog;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.mappers.MeterStatusLogMapper;
import com.elvaco.mvp.database.repository.mappers.MeterStatusLogToPredicateMapper;
import org.springframework.data.domain.PageRequest;

public class MeterStatusLogsRepository implements MeterStatusLogs {

  public final PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository;
  public final MeterStatusLogMapper meterStatusLogMapper;
  public final MeterStatusLogToPredicateMapper filterMapper;

  public MeterStatusLogsRepository(
    PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository,
    MeterStatusLogToPredicateMapper filterMapper,
    MeterStatusLogMapper meterStatusLogMapper
  ) {
    this.physicalMeterStatusLogJpaRepository = physicalMeterStatusLogJpaRepository;
    this.filterMapper = filterMapper;
    this.meterStatusLogMapper = meterStatusLogMapper;
  }

  @Override
  public Page<MeterStatusLog> findAll(Map<String, List<String>> filterParams, Pageable pageable) {
    return new PageAdapter<>(
      physicalMeterStatusLogJpaRepository.findAll(
        filterMapper.map(filterParams),
        new PageRequest(pageable.getPageNumber(), pageable.getPageSize())
      ).map(meterStatusLogMapper::toDomainModel)
    );
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
