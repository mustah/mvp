package com.elvaco.mvp.database.repository.mappers;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.database.entity.meter.DisplayQuantityEntity;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;

import lombok.RequiredArgsConstructor;

import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
public class MeterDefinitionEntityMapper {

  private final MediumEntityMapper mediumEntityMapper;
  private final DisplayQuantityEntityMapper displayQuantityEntityMapper;

  public MeterDefinitionEntity toEntity(MeterDefinition domainModel) {
    return new MeterDefinitionEntity(
      domainModel.id,
      Optional.ofNullable(domainModel.organisation)
        .map(OrganisationEntityMapper::toEntity)
        .orElse(null),
      Collections.emptySet(),
      domainModel.name,
      mediumEntityMapper.toMediumEntity(domainModel.medium),
      domainModel.autoApply
    );
  }

  public MeterDefinition toDomainModel(MeterDefinitionEntity entity) {
    return new MeterDefinition(
      entity.id,
      Optional.ofNullable(entity.organisation)
        .map(OrganisationEntityMapper::toDomainModel)
        .orElse(null),
      entity.name,
      mediumEntityMapper.toMedium(entity.medium),
      entity.autoApply,
      entity.quantities.stream()
        .map(displayQuantityEntityMapper::toDisplayQuantity)
        .collect(toSet())
    );
  }

  public Set<DisplayQuantityEntity> toDisplayQuantityEntities(
    MeterDefinition domainModel
  ) {
    return domainModel.quantities.stream()
      .map(displayQuantityEntityMapper::toDisplayQuantityEntity)
      .collect(toSet());
  }
}
