package com.elvaco.mvp.database.repository.mappers;

import java.util.stream.Collectors;

import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;
import com.elvaco.mvp.database.entity.meter.QuantityEntity;

public class MeterDefinitionMapper
  implements DomainEntityMapper<MeterDefinition, MeterDefinitionEntity> {

  @Override
  public MeterDefinition toDomainModel(MeterDefinitionEntity entity) {
    return new MeterDefinition(
      entity.id,
      entity.medium,
      entity.quantities.stream()
        .map(this::toQuantity)
        .collect(Collectors.toList())
    );
  }

  @Override
  public MeterDefinitionEntity toEntity(MeterDefinition domainModel) {
    return new MeterDefinitionEntity(
      domainModel.id,
      domainModel.quantities
        .stream()
        .map(this::toQuantityEntity)
        .collect(Collectors.toList()),
      domainModel.medium
    );
  }

  private Quantity toQuantity(QuantityEntity quantityEntity) {
    return new Quantity(quantityEntity.id, quantityEntity.name, quantityEntity.unit);
  }

  private QuantityEntity toQuantityEntity(Quantity quantity) {
    return new QuantityEntity(quantity.getId(), quantity.getName(), quantity.getUnit());
  }
}
