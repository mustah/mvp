package com.elvaco.mvp.core.access;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.Quantity;

public final class QuantityAccess {

  private final ConcurrentHashMap<String, Quantity> quantityNameToQuantityMap =
    new ConcurrentHashMap<>();

  private QuantityAccess() {}

  public static QuantityAccess singleton() {
    return SingletonHolder.INSTANCE;
  }

  public void loadAll(List<Quantity> quantities) {
    quantities.forEach(quantity -> quantityNameToQuantityMap.put(quantity.name, quantity));
  }

  @Nullable
  public Quantity getByName(String name) {
    return quantityNameToQuantityMap.get(name);
  }

  private static final class SingletonHolder {
    private static final QuantityAccess INSTANCE = new QuantityAccess();
  }
}
