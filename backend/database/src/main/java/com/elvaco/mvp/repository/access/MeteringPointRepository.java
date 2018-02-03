package com.elvaco.mvp.repository.access;

import java.util.List;
import java.util.Map;

import com.elvaco.mvp.core.domainmodels.MeteringPoint;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.usecase.MeteringPoints;
import com.elvaco.mvp.repository.jpa.MeteringPointJpaRepository;
import com.elvaco.mvp.repository.jpa.mappers.MeteringPointToPredicateMapper;
import com.elvaco.mvp.spring.PageAdapter;
import org.springframework.data.domain.PageRequest;

import static java.util.stream.Collectors.toList;

public class MeteringPointRepository implements MeteringPoints {

  private final MeteringPointJpaRepository meteringPointJpaRepository;
  private final MeteringPointMapper meteringPointMapper;
  private final MeteringPointToPredicateMapper filterMapper;

  public MeteringPointRepository(
    MeteringPointJpaRepository meteringPointJpaRepository,
    MeteringPointToPredicateMapper filterMapper,
    MeteringPointMapper meteringPointMapper
  ) {
    this.meteringPointJpaRepository = meteringPointJpaRepository;
    this.filterMapper = filterMapper;
    this.meteringPointMapper = meteringPointMapper;
  }

  @Override
  public MeteringPoint findOne(Long id) {
    return meteringPointMapper.toDomainModel(
      meteringPointJpaRepository.findOne(id)
    );
  }

  @Override
  public List<MeteringPoint> findAll() {
    return meteringPointJpaRepository.findAll()
      .stream()
      .map(meteringPointMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public Page<MeteringPoint> findAll(Map<String, List<String>> filterParams, Pageable pageable) {
    return new PageAdapter<>(
      meteringPointJpaRepository.findAll(
        filterMapper.map(filterParams),
        new PageRequest(pageable.getPageNumber(), pageable.getPageSize())
      ).map(meteringPointMapper::toDomainModel)
    );
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
