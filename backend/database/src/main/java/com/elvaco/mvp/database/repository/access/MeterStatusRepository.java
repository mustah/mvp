package com.elvaco.mvp.database.repository.access;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.MeterStatus;
import com.elvaco.mvp.core.spi.repository.MeterStatuses;
import com.elvaco.mvp.database.repository.jpa.MeterStatusJpaRepository;
import com.elvaco.mvp.database.repository.mappers.MeterStatusMapper;

import static java.util.stream.Collectors.toList;

public class MeterStatusRepository implements MeterStatuses {

  private final MeterStatusJpaRepository meterStatusJpaRepository;
  private final MeterStatusMapper meterStatusMapper;

  public MeterStatusRepository(
    MeterStatusJpaRepository meterStatusJpaRepository,
    MeterStatusMapper meterStatusMapper
  ) {
    this.meterStatusJpaRepository = meterStatusJpaRepository;
    this.meterStatusMapper = meterStatusMapper;
  }

  @Override
  public List<MeterStatus> findAll() {
    return meterStatusJpaRepository.findAll()
      .stream()
      .map(meterStatusMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public void save(List<MeterStatus> meterStatus) {
    meterStatusJpaRepository.save(
      meterStatus.stream().map(meterStatusMapper::toEntity).collect(toList())
    );
  }
}
