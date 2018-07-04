package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.QuantityPresentationInformation;
import com.elvaco.mvp.database.entity.meter.QuantityEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class QuantityEntityMapper {

  public static Quantity toDomainModel(QuantityEntity quantityEntity) {
    return new Quantity(
      quantityEntity.id,
      quantityEntity.name,
      new QuantityPresentationInformation(quantityEntity.unit, quantityEntity.seriesDisplayMode)
    );
  }

  public static QuantityEntity toEntity(Quantity quantity) {
    return new QuantityEntity(
      quantity.id,
      quantity.name,
      quantity.presentationUnit(),
      quantity.seriesDisplayMode()
    );
  }
}
