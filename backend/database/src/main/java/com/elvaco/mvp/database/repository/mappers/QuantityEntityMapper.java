package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.QuantityPresentationInformation;
import com.elvaco.mvp.database.entity.meter.QuantityEntity;

public final class QuantityEntityMapper {

  public QuantityEntityMapper(QuantityProvider quantityProvider) {
    this.quantityProvider = quantityProvider;
  }

  private final QuantityProvider quantityProvider;

  public Quantity toDomainModel(QuantityEntity quantityEntity) {
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

  public QuantityEntity toEntity(Quantity quantity) {
    return new QuantityEntity(
      quantityProvider.getId(quantity),
      quantity.name,
      quantity.presentationUnit(),
      getStorageUnit(quantity),
      quantity.seriesDisplayMode()
    );
  }

  private String getStorageUnit(Quantity inputQuantity) {
    Quantity preloadedQty = quantityProvider.getByName(inputQuantity.name);

    if (preloadedQty == null) {
      return inputQuantity.presentationUnit();
    }
    return preloadedQty.storageUnit;
  }

}
