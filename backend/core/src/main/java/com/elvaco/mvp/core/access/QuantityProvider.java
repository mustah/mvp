package com.elvaco.mvp.core.access;

import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.Quantity;

@FunctionalInterface
public interface QuantityProvider {
  @Nullable
  Quantity getByName(String name);

  default Integer getId(Quantity quantity) {
    Quantity preloadedQty = getByName(quantity.name);
    if (preloadedQty == null) {
      return quantity.id;
    }
    if (quantity.id != null && !quantity.id.equals(preloadedQty.id)) {
      throw new RuntimeException("Supplied Quantity.Id does not match previously stored Id");
    }
    return preloadedQty.id;
  }
}
