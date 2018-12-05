package com.elvaco.mvp.core.access;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.Quantity;

public final class QuantityAccess implements QuantityProvider {

  private final ConcurrentHashMap<String, Quantity> quantityNameToQuantityMap =
    new ConcurrentHashMap<>();

  public QuantityAccess(List<Quantity> quantities) {
    quantities.forEach(quantity -> quantityNameToQuantityMap.put(quantity.name, quantity));
  }

  @Override
  @Nullable
  public Quantity getByName(String name) {
    return quantityNameToQuantityMap.get(name);
  }
}
