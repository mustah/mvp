package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.access.QuantityAccess;
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
      new QuantityPresentationInformation(
        quantityEntity.displayUnit,
        quantityEntity.seriesDisplayMode
      ),
      quantityEntity.storageUnit
    );
  }

  public static QuantityEntity toEntity(Quantity quantity) {
    return new QuantityEntity(
      QuantityAccess.singleton().getId(quantity),
      quantity.name,
      quantity.presentationUnit(),
      QuantityAccess.singleton().getStorageUnit(quantity),
      quantity.seriesDisplayMode()
    );
  }
}
