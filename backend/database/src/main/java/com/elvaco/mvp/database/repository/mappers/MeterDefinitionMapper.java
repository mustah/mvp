package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.QuantityPresentationInformation;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;
import com.elvaco.mvp.database.entity.meter.QuantityEntity;

import static java.util.stream.Collectors.toSet;

public class MeterDefinitionMapper
  implements DomainEntityMapper<MeterDefinition, MeterDefinitionEntity> {

  @Override
  public MeterDefinition toDomainModel(MeterDefinitionEntity entity) {
    return new MeterDefinition(
      entity.type,
      entity.medium,
      entity.quantities.stream()
        .map(this::toQuantity)
        .collect(toSet()),
      entity.systemOwned
    );
  }

  @Override
  public MeterDefinitionEntity toEntity(MeterDefinition domainModel) {
    return new MeterDefinitionEntity(
      domainModel.type,
      domainModel.quantities
        .stream()
        .map(this::toQuantityEntity)
        .collect(toSet()),
      domainModel.medium,
      domainModel.systemOwned
    );
  }

  private Quantity toQuantity(QuantityEntity quantityEntity) {
    return new Quantity(
      quantityEntity.id,
      quantityEntity.name,
      new QuantityPresentationInformation(
        quantityEntity.unit,
        quantityEntity.seriesDisplayMode
      )
    );
  }

  private QuantityEntity toQuantityEntity(Quantity quantity) {
    return new QuantityEntity(
      quantity.id,
      quantity.name,
      quantity.defaultPresentationUnit(),
      quantity.defaultSeriesDisplayMode()
    );
  }
}
