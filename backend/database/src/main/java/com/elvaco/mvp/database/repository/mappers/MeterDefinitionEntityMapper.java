package com.elvaco.mvp.database.repository.mappers;

import java.util.Objects;
import java.util.Set;

import com.elvaco.mvp.core.access.QuantityAccess;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;
import com.elvaco.mvp.database.entity.meter.QuantityEntity;
import lombok.experimental.UtilityClass;

import static java.util.stream.Collectors.toSet;

@UtilityClass
public class MeterDefinitionEntityMapper {

  public static MeterDefinition toDomainModel(MeterDefinitionEntity entity) {
    return new MeterDefinition(
      entity.type,
      entity.medium,
      entity.quantities.stream()
        .map(QuantityEntityMapper::toDomainModel)
        .collect(toSet()),
      entity.systemOwned
    );
  }

  public static MeterDefinitionEntity toEntity(MeterDefinition domainModel) {
    return new MeterDefinitionEntity(
      domainModel.type,
      toQuantityEntities(domainModel),
      domainModel.medium,
      domainModel.systemOwned
    );
  }

  private static Set<QuantityEntity> toQuantityEntities(MeterDefinition domainModel) {
    return domainModel.quantities.stream()
      .map(quantity -> QuantityAccess.singleton().getByName(quantity.name))
      .filter(Objects::nonNull)
      .map(QuantityEntityMapper::toEntity)
      .collect(toSet());
  }
}
