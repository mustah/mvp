package com.elvaco.mvp.database.repository.access;

import java.util.List;

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

    if (meterDefinition.systemOwned) {
      meterDefinitionJpaRepository.findByMedium(meterDefinition.medium)
        .ifPresent(systemOwned -> entity.type = systemOwned.type);
    }
    return meterDefinitionEntityMapper.toDomainModel(meterDefinitionJpaRepository.save(entity));
  }

  @Override
  public List<MeterDefinition> findAll() {
    return meterDefinitionJpaRepository.findAll().stream()
      .map(meterDefinitionEntityMapper::toDomainModel)
      .collect(toList());
  }
}
