package com.elvaco.mvp.core.spi.repository;

import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Property;

public interface Properties {

  Property save(Property property);

  Optional<Property> findById(Property.Id id);

  void deleteById(Property.Id id);
}
