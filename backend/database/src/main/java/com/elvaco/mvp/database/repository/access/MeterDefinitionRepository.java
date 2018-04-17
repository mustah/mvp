package com.elvaco.mvp.database.repository.access;

import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;
import com.elvaco.mvp.database.repository.jpa.MeterDefinitionJpaRepository;
import com.elvaco.mvp.database.repository.mappers.MeterDefinitionMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MeterDefinitionRepository implements MeterDefinitions {

  private final MeterDefinitionJpaRepository meterDefinitionJpaRepository;

  @Override
  public MeterDefinition save(MeterDefinition meterDefinition) {
    MeterDefinitionEntity entity = MeterDefinitionMapper.toEntity(meterDefinition);

    if (meterDefinition.systemOwned) {
      meterDefinitionJpaRepository.findByMedium(meterDefinition.medium)
        .ifPresent(systemOwned -> entity.type = systemOwned.type);
    }
    return MeterDefinitionMapper.toDomainModel(meterDefinitionJpaRepository.save(entity));
  }
}
