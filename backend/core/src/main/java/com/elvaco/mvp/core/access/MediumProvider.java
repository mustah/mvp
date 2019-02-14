package com.elvaco.mvp.core.access;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.exception.NoSuchMedium;

import static java.util.Collections.emptyList;

@FunctionalInterface
public interface MediumProvider {
  Optional<Medium> getByName(String name);

  default Medium getByNameOrThrow(String name) {
    return getByName(name).orElseThrow(() -> new NoSuchMedium(name));
  }

  default List<Medium> all() {
    return emptyList();
  }
}
