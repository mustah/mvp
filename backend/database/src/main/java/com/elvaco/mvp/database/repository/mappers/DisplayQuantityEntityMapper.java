package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.DisplayQuantity;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.database.entity.meter.DisplayQuantityEntity;
import com.elvaco.mvp.database.entity.meter.DisplayQuantityPk;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DisplayQuantityEntityMapper {
  private final QuantityEntityMapper quantityEntityMapper;

  public DisplayQuantityEntity toDisplayQuantityEntity(
    DisplayQuantity displayQuantity
  ) {
    DisplayQuantityPk pk = new DisplayQuantityPk(
      quantityEntityMapper.toEntity(displayQuantity.quantity),
      null,
      displayQuantity.displayMode
    );
    return new DisplayQuantityEntity(
      pk,
      displayQuantity.unit,
      displayQuantity.decimals
    );
  }

  public DisplayQuantity toDisplayQuantity(
    DisplayQuantityEntity displayQuantityEntity
  ) {
    DisplayQuantityPk displayQuantityPk = displayQuantityEntity.getId();
    Quantity quantity = quantityEntityMapper.toDomainModel(displayQuantityPk.quantity);

    return new DisplayQuantity(
      quantity,
      displayQuantityPk.displayMode,
      displayQuantityEntity.decimals,
      displayQuantityEntity.displayUnit
    );
  }
}
