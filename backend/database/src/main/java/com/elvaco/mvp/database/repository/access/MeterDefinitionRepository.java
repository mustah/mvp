package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;
import com.elvaco.mvp.database.repository.jpa.MeterDefinitionJpaRepository;
import com.elvaco.mvp.database.repository.mappers.MeterDefinitionEntityMapper;

import lombok.RequiredArgsConstructor;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class MeterDefinitionRepository implements MeterDefinitions {

  private final MeterDefinitionJpaRepository meterDefinitionJpaRepository;
  private final MeterDefinitionEntityMapper meterDefinitionEntityMapper;

  @Override
  public MeterDefinition save(MeterDefinition meterDefinition) {
    MeterDefinitionEntity entity = meterDefinitionEntityMapper.toEntity(meterDefinition);
    return meterDefinitionEntityMapper.toDomainModel(meterDefinitionJpaRepository.save(entity));
  }

  @Override
  public Optional<MeterDefinition> findById(Long id) {
    return meterDefinitionJpaRepository.findById(id)
      .map(meterDefinitionEntityMapper::toDomainModel);
  }

  @Override
  public List<MeterDefinition> findAll() {
    return meterDefinitionJpaRepository.findAll().stream()
      .map(meterDefinitionEntityMapper::toDomainModel)
      .collect(toList());
  }
}
