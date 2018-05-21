package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.QuantityPresentationInformation;
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
        .map(MeterDefinitionEntityMapper::toQuantity)
        .collect(toSet()),
      entity.systemOwned
    );
  }

  public static MeterDefinitionEntity toEntity(MeterDefinition domainModel) {
    return new MeterDefinitionEntity(
      domainModel.type,
      domainModel.quantities
        .stream()
        .map(MeterDefinitionEntityMapper::toQuantityEntity)
        .collect(toSet()),
      domainModel.medium,
      domainModel.systemOwned
    );
  }

  private static Quantity toQuantity(QuantityEntity quantityEntity) {
    return new Quantity(
      quantityEntity.id,
      quantityEntity.name,
      new QuantityPresentationInformation(
        quantityEntity.unit,
        quantityEntity.seriesDisplayMode
      )
    );
  }

  private static QuantityEntity toQuantityEntity(Quantity quantity) {
    return new QuantityEntity(
      quantity.id,
      quantity.name,
      quantity.presentationUnit(),
      quantity.seriesDisplayMode()
    );
  }
}
