package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.access.QuantityProvider;
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
      quantityEntity.storageUnit
    );
  }

  public QuantityEntity toEntity(Quantity quantity) {
    return new QuantityEntity(
      quantityProvider.getId(quantity),
      quantity.name,
      getStorageUnit(quantity)
    );
  }

  private String getStorageUnit(Quantity inputQuantity) {
    return quantityProvider.getByName(inputQuantity.name)
      .map(q -> q.storageUnit)
      .orElse(inputQuantity.storageUnit);
  }
}
