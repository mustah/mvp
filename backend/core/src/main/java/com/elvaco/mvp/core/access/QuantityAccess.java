package com.elvaco.mvp.core.access;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.elvaco.mvp.core.domainmodels.Quantity;

import static java.util.stream.Collectors.toList;

public final class QuantityAccess implements QuantityProvider {

  private final ConcurrentHashMap<String, Quantity>
    quantityMap = new ConcurrentHashMap<>();

  public QuantityAccess(List<Quantity> quantities) {
    quantities.forEach(quantity -> quantityMap.put(quantity.name, quantity));
  }

  @Override
  public Optional<Quantity> getByName(String name) {
    return Optional.ofNullable(quantityMap.get(name));
  }

  @Override
  public List<Quantity> all() {
    return new ArrayList<>(quantityMap.values());
  }
}
