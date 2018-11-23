package com.elvaco.mvp.core.access;

import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.Quantity;

@FunctionalInterface
public interface QuantityProvider {
  @Nullable
  Quantity getByName(String name);
}
