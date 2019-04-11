package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.domainmodels.DisplayMode;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.database.entity.meter.QuantityEntity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QuantityEntityMapper {

  private final QuantityProvider quantityProvider;

  public Quantity toDomainModel(QuantityEntity quantityEntity) {
    return new Quantity(
      quantityEntity.id,
      quantityEntity.name,
      quantityEntity.storageUnit,
      quantityEntity.storageMode
    );
  }

  public QuantityEntity toEntity(Quantity quantity) {
    return new QuantityEntity(
      quantityProvider.getId(quantity),
      quantity.name,
      getStorageUnit(quantity),
      getStorageMode(quantity)
    );
  }

  private String getStorageUnit(Quantity inputQuantity) {
    return quantityProvider.getByName(inputQuantity.name)
      .map(q -> q.storageUnit)
      .orElse(inputQuantity.storageUnit);
  }

  private DisplayMode getStorageMode(Quantity inputQuantity) {
    return quantityProvider.getByName(inputQuantity.name)
      .map(q -> q.storageMode)
      .orElse(inputQuantity.storageMode);
  }
}
