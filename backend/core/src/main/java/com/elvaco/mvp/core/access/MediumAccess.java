package com.elvaco.mvp.core.access;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.elvaco.mvp.core.domainmodels.Medium;

public class MediumAccess implements MediumProvider {
  private final Map<String, Medium> mediumMap;

  public MediumAccess(Collection<Medium> media) {
    mediumMap = media.stream().collect(Collectors.toMap(medium -> medium.name, medium -> medium));
  }

  @Override
  public Optional<Medium> getByName(String name) {
    return Optional.ofNullable(mediumMap.get(name));
  }
}
