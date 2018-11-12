package com.elvaco.mvp.database.repository.mappers;

import java.util.Objects;

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
      ensureIdMatch(quantity),
      quantity.name,
      quantity.presentationUnit(),
      getStorageUnit(quantity),
      quantity.seriesDisplayMode()
    );
  }

  private static String getStorageUnit(Quantity quantity) {
    Quantity preloadedQty = QuantityAccess.singleton().getByName(quantity.name);

    if (preloadedQty == null) {
      return quantity.presentationUnit();
    }
    return preloadedQty.storageUnit;
  }

  private static Integer ensureIdMatch(Quantity quantity) {
    Quantity preloadedQty = QuantityAccess.singleton().getByName(quantity.name);
    if (preloadedQty == null) {
      return quantity.id;
    }
    if (!Objects.equals(preloadedQty.id, quantity.id)) {
      throw new RuntimeException("Supplied Qunatity.Id does not match previously stored Id");
    }
    return preloadedQty.id;
  }
}
