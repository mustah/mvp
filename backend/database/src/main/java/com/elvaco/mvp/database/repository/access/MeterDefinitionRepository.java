package com.elvaco.mvp.database.repository.access;

import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.database.repository.jpa.MeterDefinitionJpaRepository;
import com.elvaco.mvp.database.repository.mappers.MeterDefinitionMapper;

public class MeterDefinitionRepository implements MeterDefinitions {

  private final MeterDefinitionJpaRepository meterDefinitionJpaRepository;
  private final MeterDefinitionMapper meterDefinitionMapper;

  public MeterDefinitionRepository(
    MeterDefinitionJpaRepository meterDefinitionJpaRepository,
    MeterDefinitionMapper meterDefinitionMapper
  ) {
    this.meterDefinitionJpaRepository = meterDefinitionJpaRepository;
    this.meterDefinitionMapper = meterDefinitionMapper;
  }

  @Override
  public MeterDefinition save(MeterDefinition meterDefinition) {
    return meterDefinitionMapper.toDomainModel(
      meterDefinitionJpaRepository.save(meterDefinitionMapper.toEntity(meterDefinition))
    );
  }
}
