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

  public String getStorageUnit(Quantity quantity) {
    Quantity preloadedQty = getByName(quantity.name);

    if (preloadedQty == null) {
      return quantity.presentationUnit();
    }
    return preloadedQty.storageUnit;
  }

  public Integer getId(Quantity quantity) {
    Quantity preloadedQty = getByName(quantity.name);
    if (preloadedQty == null) {
      return quantity.id;
    }
    if (quantity.id != null && !quantity.id.equals(preloadedQty.id)) {
      throw new RuntimeException("Supplied Qunatity.Id does not match previously stored Id");
    }
    return preloadedQty.id;
  }

  private static final class SingletonHolder {
    private static final QuantityAccess INSTANCE = new QuantityAccess();
  }
}
