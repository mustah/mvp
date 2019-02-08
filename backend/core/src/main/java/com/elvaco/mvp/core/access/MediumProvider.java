package com.elvaco.mvp.core.access;

import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.exception.NoSuchMedium;

@FunctionalInterface
public interface MediumProvider {
  Optional<Medium> getByName(String name);

  default Medium getByNameOrThrow(String name) {
    return getByName(name).orElseThrow(() -> new NoSuchMedium(name));
  }
}
