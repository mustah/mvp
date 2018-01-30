package com.elvaco.mvp.repository.access;

import java.util.List;
import java.util.stream.Collectors;

import com.elvaco.mvp.core.domainmodels.MeteringPoint;
import com.elvaco.mvp.core.usecase.MeteringPoints;
import com.elvaco.mvp.repository.jpa.MeteringPointJpaRepository;

public class MeteringPointRepository implements MeteringPoints {

  private final MeteringPointJpaRepository meteringPointJpaRepository;
  private final MeteringPointMapper meteringPointMapper;

  public MeteringPointRepository(
    MeteringPointJpaRepository meteringPointJpaRepository,
    MeteringPointMapper meteringPointMapper
  ) {
    this.meteringPointJpaRepository = meteringPointJpaRepository;
    this.meteringPointMapper = meteringPointMapper;
  }

  @Override
  public List<MeteringPoint> findAll() {
    return meteringPointJpaRepository.findAll()
      .stream()
      .map(meteringPointMapper::toDomainModel)
      .collect(Collectors.toList());
  }

  @Override
  public void save(MeteringPoint meteringPoint) {
    meteringPointJpaRepository.save(
      meteringPointMapper.toEntity(meteringPoint)
    );
  }

  @Override
  public void deleteAll() {
    meteringPointJpaRepository.deleteAll();
  }
}
