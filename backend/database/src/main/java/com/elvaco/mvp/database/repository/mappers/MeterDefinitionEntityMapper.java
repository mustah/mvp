package com.elvaco.mvp.database.repository.mappers;

import java.util.Objects;
import java.util.Set;

import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;
import com.elvaco.mvp.database.entity.meter.QuantityEntity;
import lombok.RequiredArgsConstructor;

import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
public class MeterDefinitionEntityMapper {

  private final QuantityEntityMapper quantityEntityMapper;
  private final QuantityProvider quantityProvider;

  public MeterDefinitionEntity toEntity(MeterDefinition domainModel) {
    return new MeterDefinitionEntity(
      domainModel.type,
      toQuantityEntities(domainModel),
      domainModel.medium,
      domainModel.systemOwned
    );
  }

  public MeterDefinition toDomainModel(MeterDefinitionEntity entity) {
    return new MeterDefinition(
      entity.type,
      entity.medium,
      entity.quantities.stream()
        .map(quantityEntityMapper::toDomainModel)
        .collect(toSet()),
      entity.systemOwned
    );
  }

  private Set<QuantityEntity> toQuantityEntities(MeterDefinition domainModel) {
    return domainModel.quantities.stream()
      .map(quantity -> quantityProvider.getByName(quantity.name))
      .filter(Objects::nonNull)
      .map(quantityEntityMapper::toEntity)
      .collect(toSet());
  }
}
