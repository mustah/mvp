package com.elvaco.mvp.core.access;

import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.exception.NoSuchQuantity;

@FunctionalInterface
public interface QuantityProvider {

  Optional<Quantity> getByName(String name);

  default Quantity getByNameOrThrow(String name) {
    return getByName(name).orElseThrow(() -> new NoSuchQuantity(name));
  }

  default Integer getId(Quantity quantity) {
    Quantity preloadedQty = getByName(quantity.name).orElse(quantity);
    if (quantity.id != null && !quantity.id.equals(preloadedQty.id)) {
      throw new RuntimeException(String.format(
        "Supplied Quantity.Id '%d' does not match previously stored Id '%d'",
        quantity.id,
        preloadedQty.id
      ));
    }
    return preloadedQty.id;
  }
}
